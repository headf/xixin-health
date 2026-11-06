<template>
  <div class="fade-in" style="height:100%;display:flex;flex-direction:column">
    <div class="page-hd">
      <div class="page-hd-left">
        <h2>在线咨询</h2>
        <p class="desc">AI 健康助手为您解答体检相关问题</p>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-area" ref="chatArea">
      <div class="msg-date" v-if="messages.length > 0">今日</div>

      <div v-for="(msg, idx) in messages" :key="idx"
        :class="['msg-row', msg.type === 'me' ? 'msg-me' : msg.type === 'reply' ? 'msg-reply' : 'msg-system']">

        <div v-if="msg.type === 'system'" class="msg-system-text">{{ msg.text }}</div>

        <template v-else-if="msg.type === 'reply'">
          <div class="reply-avatar">💁</div>
          <div class="reply-bubble">
            <div class="reply-name">健康助手</div>
            <div v-if="!msg.loading" class="reply-text" v-html="msg.text"></div>
            <div v-else class="reply-text loading">思考中...</div>
          </div>
        </template>

        <template v-else>
          <div class="my-bubble">
            <div class="my-text">{{ msg.text }}</div>
          </div>
        </template>
      </div>

      <div ref="scrollBottom"></div>
    </div>

    <!-- 快捷问题 -->
    <div class="quick-replies" v-if="showQuick">
      <div class="quick-title">常见问题</div>
      <div class="quick-tags">
        <span class="quick-tag" v-for="(q, i) in quickQuestions" :key="i" @click="sendQuick(q)">{{ q }}</span>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="input-bar">
      <input class="chat-input" v-model="inputText" placeholder="输入您的问题..." @keyup.enter="sendText" :disabled="waitingReply" />
      <button class="send-btn" :class="{ disabled: waitingReply }" @click="sendText">{{ waitingReply ? '...' : '发送' }}</button>
    </div>
  </div>
</template>

<script>
import { appointmentApi, reportApi } from '../../common/api.js'

const API_KEY = 'your-api-key-here'

export default {
  name: 'PageConsult',
  data() {
    return {
      messages: [{ type: 'system', text: '正在加载您的健康数据...' }],
      inputText: '',
      showQuick: true,
      waitingReply: false,
      userContext: '',
      quickQuestions: ['我最近的体检情况怎么样？', '体检前需要注意什么？', '报告指标怎么看？', '有什么健康建议？']
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    async initData() {
      const userId = localStorage.getItem('userId')
      let context = ''
      try {
        if (userId) {
          const res = await fetch('/api/v1/appointments/user/' + userId)
          const json = await res.json()
          const apps = json.code === '0' ? (json.data || []) : []
          if (apps.length > 0) {
            context += '【预约信息】\n'
            apps.slice(0, 3).forEach(a => {
              context += `- 预约日期: ${a.appointmentDate || ''} ${a.timeSlot || ''}, 状态: ${a.status || ''}\n`
            })
            context += '\n'
          }

          const reportRes = await fetch('/api/v1/reports/by-user/' + userId)
          const reportJson = await reportRes.json()
          const reports = reportJson.code === '0' ? (reportJson.data || []) : []
          if (reports.length > 0) {
            const latest = reports[0]
            context += '【最新报告摘要】\n'
            context += `- 报告编号: ${latest.reportNo || ''}\n`
            context += `- 异常等级: ${latest.abnormalStatus || ''}\n`
            context += `- 诊断摘要: ${latest.summary || '无'}\n`
            if (latest.doctorAdvice) context += `- 医生建议: ${latest.doctorAdvice}\n`
            context += '\n'
          }
        }
      } catch (e) { console.error('加载用户数据失败', e) }

      this.userContext = context
      this.messages[0] = {
        type: 'system',
        text: context ? '您好！已加载您的体检数据，有什么可以帮您的？' : '您好！我是 AI 健康助手，有什么可以帮您的？'
      }
      this.$nextTick(() => this.scrollToBottom())
    },

    sendQuick(q) { this.showQuick = false; this.sendMessage(q) },
    sendText() {
      const text = this.inputText.trim()
      if (!text || this.waitingReply) return
      this.showQuick = false
      this.inputText = ''
      this.sendMessage(text)
    },

    async sendMessage(text) {
      this.messages.push({ type: 'me', text })
      this.$nextTick(() => this.scrollToBottom())

      this.waitingReply = true
      const loadingIdx = this.messages.length
      this.messages.push({ type: 'reply', text: '', loading: true })
      this.$nextTick(() => this.scrollToBottom())

      try {
        const reply = await this.callDeepSeek(text)
        this.messages[loadingIdx].text = reply
        this.messages[loadingIdx].loading = false
      } catch {
        this.messages[loadingIdx].text = '抱歉，我暂时无法回复，请稍后再试。'
        this.messages[loadingIdx].loading = false
      }
      this.waitingReply = false
      this.$nextTick(() => this.scrollToBottom())
    },

    async callDeepSeek(prompt) {
      const history = this.messages
        .filter(m => m.type === 'me' || (m.type === 'reply' && !m.loading && m.text))
        .slice(-10)
        .map(m => ({ role: m.type === 'me' ? 'user' : 'assistant', content: m.text }))

      const systemContent = '你是一个专业的体检健康咨询助手，用中文回答。请直接用HTML格式回复，不要用markdown。'
        + '使用 <p>、<strong>、<ul><li>、<br/> 等标签。不要用 ```、** 等markdown语法。'
        + (this.userContext ? '\n\n以下是该用户的体检数据，请根据这些数据回答：\n' + this.userContext : '')

      const res = await fetch('https://api.deepseek.com/chat/completions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + API_KEY },
        body: JSON.stringify({
          model: 'deepseek-chat',
          messages: [
            { role: 'system', content: systemContent },
            ...history,
            { role: 'user', content: prompt }
          ],
          temperature: 0.7,
          max_tokens: 800
        })
      })
      const data = await res.json()
      if (data.choices && data.choices.length > 0) return data.choices[0].message.content
      throw new Error(data.error?.message)
    },

    scrollToBottom() {
      const el = this.$refs.scrollBottom
      if (el) el.scrollIntoView({ behavior: 'smooth' })
    }
  }
}
</script>

<style scoped>
.chat-area {
  flex: 1; overflow-y: auto; padding: 16px 24px;
  background: #f8fafc; border-radius: 12px;
  margin: 0 20px 12px;
}
.msg-date { text-align: center; font-size: 12px; color: #94a3b8; margin-bottom: 16px; }
.msg-row { margin-bottom: 16px; display: flex; }
.msg-system { justify-content: center; }
.msg-reply { justify-content: flex-start; align-items: flex-start; }
.msg-me { justify-content: flex-end; }
.msg-system-text { font-size: 12px; color: #94a3b8; background: #fff; padding: 6px 16px; border-radius: 999px; }
.reply-avatar { width: 36px; height: 36px; border-radius: 50%; background: #fbe1d1; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; margin-right: 10px; }
.reply-bubble { max-width: 70%; background: #fff; border-radius: 12px 12px 12px 4px; padding: 12px 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.reply-name { font-size: 12px; color: #94a3b8; margin-bottom: 6px; }
.reply-text { font-size: 14px; color: #1e293b; line-height: 1.7; }
.reply-text.loading { color: #94a3b8; font-style: italic; }
.reply-text :deep(p) { margin-bottom: 8px; }
.reply-text :deep(ul) { padding-left: 20px; margin-bottom: 8px; }
.reply-text :deep(li) { margin-bottom: 4px; }
.my-bubble { max-width: 70%; background: var(--primary); border-radius: 12px 12px 4px 12px; padding: 10px 16px; }
.my-text { font-size: 14px; color: #fff; line-height: 1.5; white-space: pre-wrap; }
.quick-replies { padding: 8px 24px 4px; margin: 0 20px; }
.quick-title { font-size: 12px; color: #94a3b8; margin-bottom: 8px; }
.quick-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.quick-tag { font-size: 12px; color: #1e293b; background: #f1f5f9; padding: 6px 14px; border-radius: 999px; cursor: pointer; transition: background 0.12s; user-select: none; }
.quick-tag:hover { background: var(--primary-light); color: var(--primary); }
.input-bar { display: flex; align-items: center; gap: 8px; padding: 12px 20px 20px; }
.chat-input { flex: 1; height: 42px; background: #f1f5f9; border-radius: 21px; padding: 0 18px; font-size: 14px; border: none; outline: none; transition: all 0.12s; font-family: inherit; }
.chat-input:focus { background: #fff; box-shadow: 0 0 0 2px var(--primary-light); }
.send-btn { height: 36px; padding: 0 20px; border: none; border-radius: 18px; background: var(--primary); color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.12s; }
.send-btn:hover { background: var(--primary-dark); }
.send-btn:active { transform: scale(0.97); }
.send-btn.disabled { opacity: 0.5; cursor: not-allowed; }
</style>
