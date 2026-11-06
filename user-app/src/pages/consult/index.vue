<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">在线咨询</text>
			<view class="header-spacer"></view>
		</view>

		<scroll-view class="msg-area" scroll-y :scroll-top="scrollTop">
			<view class="msg-date" v-if="messages.length > 0">今日</view>

			<view :class="'msg-row ' + (msg.type === 'me' ? 'me' : msg.type === 'reply' ? 'reply' : 'system')" v-for="(msg, idx) in messages" :key="idx">
				<template v-if="msg.type === 'system'">
					<text class="system-text">{{ msg.text }}</text>
				</template>
				<template v-else-if="msg.type === 'reply'">
					<view class="reply-avatar">💁</view>
					<view class="reply-bubble">
						<text class="reply-name">健康助手</text>
						<rich-text class="reply-html" v-if="!msg.loading" :nodes="msg.text"></rich-text>
						<text class="reply-text loading" v-else>思考中...</text>
					</view>
				</template>
				<template v-else>
					<view class="my-bubble">
						<text class="my-text">{{ msg.text }}</text>
					</view>
				</template>
			</view>
		</scroll-view>

		<view class="quick-replies" v-if="showQuick">
			<text class="quick-title">常见问题</text>
			<view class="quick-tags">
				<text class="quick-tag" v-for="(q, i) in quickQuestions" :key="i" @tap="sendQuick(q)">{{ q }}</text>
			</view>
		</view>

		<view class="input-bar">
			<input class="input-field" v-model="inputText" placeholder="输入您的问题..." @confirm="sendText" :disabled="waitingReply" />
			<text class="send-btn" :class="{ disabled: waitingReply }" @tap="sendText">{{ waitingReply ? '...' : '发送' }}</text>
		</view>
	</view>
</template>

<script>
import { appointmentApi, reportApi, guideApi, packageApi } from '../../common/api.js'

const API_KEY = 'your-api-key-here'

export default {
	data() {
		return {
			messages: [{ type: 'system', text: '正在加载您的健康数据...' }],
			inputText: '',
			showQuick: true,
			scrollTop: 0,
			waitingReply: false,
			userContext: '',
			quickQuestions: ['我最近的体检情况怎么样？', '导诊路线是什么？', '体检前需要注意什么？', '报告指标怎么看？']
		}
	},
	onShow() {
		uni.setNavigationBarColor({ frontColor: '#000000', backgroundColor: '#ffffff' })
		this.initData()
	},
	methods: {
		goBack() { uni.navigateBack() },

		async initData() {
			const userId = uni.getStorageSync('userId')
			let context = ''
			try {
				// 加载预约信息
				if (userId) {
					const apps = await appointmentApi.getByUser(userId).catch(() => [])
					if (apps && apps.length > 0) {
						context += '【预约信息】\n'
						const recent = apps.slice(0, 3)
						for (const a of recent) {
							context += `- 预约日期: ${a.appointmentDate} ${a.timeSlot}, 状态: ${a.status}\n`
						}
						context += '\n'

						// 尝试加载导诊
						try {
							let guide = null
							guide = await guideApi.getByAppointment(apps[0].id)
							if (!guide && userId) {
								guide = await guideApi.getByUser(userId)
							}
							if (guide && guide.routeText) {
								context += '【导诊路线】\n' + guide.routeText + '\n\n'
							}
						} catch (e) { /* ignore */ }
					}
				}

				// 加载报告信息
				if (userId) {
					const reports = await reportApi.getByUser(userId).catch(() => [])
					if (reports && reports.length > 0) {
						const latest = reports[0]
						context += '【最新报告摘要】\n'
						context += `- 报告编号: ${latest.reportNo || ''}\n`
						context += `- 异常等级: ${latest.abnormalStatus || ''}\n`
						context += `- 诊断摘要: ${latest.summary || '无'}\n`
						if (latest.doctorAdvice) context += `- 医生建议: ${latest.doctorAdvice}\n`
						context += '\n'

						// 加载详细指标
						if (latest.examRecordId) {
							try {
								const results = await reportApi.getResults(latest.examRecordId)
								if (results && results.length > 0) {
									context += '【检查项目结果】\n'
									for (const r of results.slice(0, 20)) {
										const val = r.numericValue != null ? r.numericValue + (r.unit || '') : (r.textValue || '')
										const ref = r.referenceMin != null ? ` (参考: ${r.referenceMin}-${r.referenceMax})` : ''
										context += `- ${r.itemName}: ${val}${ref}\n`
									}
								}
							} catch (e) { /* ignore */ }
						}
					}
				}
			} catch (e) { console.error('加载用户数据失败', e) }

			this.userContext = context
			this.messages[0] = {
				type: 'system',
				text: context ? '您好！已加载您的体检数据，有什么可以帮您的？' : '您好！我是 AI 健康助手，有什么可以帮您的？'
			}
			this.scrollToBottom()
		},

		sendQuick(q) { this.showQuick = false; this.sendMessage(q) },
		sendText() {
			const text = this.inputText.trim()
			if (!text || this.waitingReply) return
			this.showQuick = false; this.inputText = ''
			this.sendMessage(text)
		},

		async sendMessage(text) {
			this.messages.push({ type: 'me', text })
			this.scrollToBottom()

			this.waitingReply = true
			const loadingIdx = this.messages.length
			this.messages.push({ type: 'reply', text: '', loading: true })
			this.scrollToBottom()

			try {
				const reply = await this.callDeepSeek(text)
				this.messages[loadingIdx].text = reply
				this.messages[loadingIdx].loading = false
			} catch (e) {
				this.messages[loadingIdx].text = '抱歉，我暂时无法回复，请稍后再试。'
				this.messages[loadingIdx].loading = false
			}
			this.waitingReply = false
			this.scrollToBottom()
		},

		async callDeepSeek(prompt) {
			const history = this.messages
				.filter(m => m.type === 'me' || (m.type === 'reply' && !m.loading && m.text))
				.slice(-10)
				.map(m => ({ role: m.type === 'me' ? 'user' : 'assistant', content: m.text }))

			const systemContent = '你是一个专业的体检健康咨询助手，用中文回答。请直接用HTML格式回复，不要用markdown。'
				+ '使用 <p>、<strong>、<ul><li>、<br/> 等标签。不要用 ```、** 等markdown语法。'
				+ (this.userContext ? '\n\n以下是该用户的体检数据，请根据这些数据回答：\n' + this.userContext : '')

			const res = await uni.request({
				url: 'https://api.deepseek.com/chat/completions',
				method: 'POST',
				header: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + API_KEY },
				data: {
					model: 'deepseek-chat',
					messages: [
						{ role: 'system', content: systemContent },
						...history,
						{ role: 'user', content: prompt }
					],
					temperature: 0.7, max_tokens: 800
				}
			})
			const data = res.data
			if (data.choices && data.choices.length > 0) return data.choices[0].message.content
			throw new Error(data.error?.message)
		},

		scrollToBottom() {
			this.$nextTick(() => { setTimeout(() => { this.scrollTop = 99999 }, 150) })
		},
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f7f7f8; display: flex; flex-direction: column; padding-top: calc(44px); }
.header { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 16rpx 32rpx; background: #ffffff; position: relative; }
.back-btn { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; padding: 12rpx 8rpx; }
.back-btn:active { opacity: 0.6; }
.header-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 40rpx; font-weight: 400; color: #17191c; position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap; }
.header-spacer { width: 80rpx; }
.msg-area { flex: 1; padding: 24rpx 32rpx 16rpx; height: 0; }
.msg-date { text-align: center; font-size: 22rpx; color: #a3a6af; margin-bottom: 20rpx; }
.msg-row { margin-bottom: 24rpx; display: flex; flex-direction: row; }
.msg-row.system { justify-content: center; }
.msg-row.reply { justify-content: flex-start; }
.msg-row.me { justify-content: flex-end; }
.system-text { font-size: 24rpx; color: #a3a6af; background: #ffffff; padding: 12rpx 24rpx; border-radius: 9999px; }
.reply-avatar { width: 64rpx; height: 64rpx; border-radius: 32rpx; background: #fbe1d1; display: flex; align-items: center; justify-content: center; font-size: 32rpx; margin-right: 16rpx; flex-shrink: 0; }
.reply-bubble { max-width: 520rpx; background: #ffffff; border-radius: 24rpx 24rpx 24rpx 8rpx; padding: 20rpx 24rpx; box-shadow: rgba(4,23,43,0.05) 0px 0px 0px 1px; margin-left: 16rpx; }
.reply-name { font-size: 22rpx; color: #777b86; display: block; margin-bottom: 8rpx; }
.reply-text { font-size: 28rpx; color: #17191c; line-height: 1.6; white-space: pre-wrap; }
.reply-text.loading { color: #a3a6af; font-style: italic; }
.reply-html { font-size: 28rpx; color: #17191c; line-height: 1.7; }
.my-bubble { max-width: 520rpx; background: #17191c; border-radius: 24rpx 24rpx 8rpx 24rpx; padding: 20rpx 24rpx; }
.my-text { font-size: 28rpx; color: #ffffff; line-height: 1.5; white-space: pre-wrap; }
.quick-replies { padding: 16rpx 32rpx; background: #ffffff; border-top: 1px solid #f0f0f0; }
.quick-title { font-size: 24rpx; color: #777b86; margin-bottom: 12rpx; display: block; }
.quick-tags { display: flex; flex-direction: row; flex-wrap: wrap; gap: 12rpx; }
.quick-tag { font-size: 24rpx; color: #17191c; background: #f7f7f8; padding: 12rpx 24rpx; border-radius: 9999px; }
.quick-tag:active { opacity: 0.7; }
.input-bar { display: flex; flex-direction: row; align-items: center; padding: 16rpx 32rpx 32rpx; background: #ffffff; border-top: 1px solid #f0f0f0; }
.input-field { flex: 1; height: 72rpx; background: #f7f7f8; border-radius: 36rpx; padding: 0 28rpx; font-size: 28rpx; color: #17191c; }
.send-btn { font-size: 28rpx; font-weight: 450; color: #17191c; padding: 16rpx 20rpx; margin-left: 12rpx; }
.send-btn:active { opacity: 0.6; }
.send-btn.disabled { color: #a3a6af; }
</style>
