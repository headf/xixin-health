<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">检前问卷</text>
			<view class="header-spacer"></view>
		</view>

		<scroll-view class="form-scroll" scroll-y>
			<!-- 温馨提示 -->
			<view class="tip-card">
				<text class="tip-title">温馨提示</text>
				<text class="tip-text">为了更准确地评估您的健康状况，请如实填写以下信息，所有数据将严格保密。</text>
			</view>

			<!-- 家族病史 -->
			<view class="section">
				<text class="section-title">家族病史</text>
				<textarea class="text-input" v-model="form.familyDiseaseHistory" placeholder='如：父母有高血压病史、家族糖尿病史等，若无请填“无”' maxlength="500" />
			</view>

			<!-- 既往病史 -->
			<view class="section">
				<text class="section-title">既往病史</text>
				<textarea class="text-input" v-model="form.pastMedicalHistory" placeholder='如：曾患肺结核、肝炎等，若无请填【无】' maxlength="500" />
			</view>

			<!-- 用药情况 -->
			<view class="section">
				<text class="section-title">用药情况</text>
				<textarea class="text-input" v-model="form.medicationHistory" placeholder='如：长期服用降压药、降糖药等，若无请填“无”' maxlength="500" />
			</view>

			<!-- 手术史 -->
			<view class="section">
				<text class="section-title">手术史</text>
				<textarea class="text-input" v-model="form.surgicalHistory" placeholder='如：2018年行阑尾切除术等，若无请填【无】' maxlength="500" />
			</view>

			<!-- 过敏史 -->
			<view class="section">
				<text class="section-title">过敏史</text>
				<textarea class="text-input" v-model="form.allergicHistory" placeholder='如：青霉素过敏、花粉过敏等，若无请填【无】' maxlength="500" />
			</view>

			<!-- 吸烟史 -->
			<view class="section">
				<text class="section-title">吸烟史</text>
				<view class="radio-group">
					<view class="radio-item" v-for="item in smokingOptions" :key="item.value" @tap="selectSmoking(item.value)">
						<view class="radio-circle" :class="{ active: form.smokingHistory === item.value }">
							<view v-if="form.smokingHistory === item.value" class="radio-dot"></view>
						</view>
						<text class="radio-label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<!-- 饮酒史 -->
			<view class="section">
				<text class="section-title">饮酒史</text>
				<view class="radio-group">
					<view class="radio-item" v-for="item in drinkingOptions" :key="item.value" @tap="selectDrinking(item.value)">
						<view class="radio-circle" :class="{ active: form.drinkingHistory === item.value }">
							<view v-if="form.drinkingHistory === item.value" class="radio-dot"></view>
						</view>
						<text class="radio-label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<!-- 运动习惯 -->
			<view class="section">
				<text class="section-title">运动习惯</text>
				<view class="radio-group">
					<view class="radio-item" v-for="item in exerciseOptions" :key="item.value" @tap="selectExercise(item.value)">
						<view class="radio-circle" :class="{ active: form.exerciseHabit === item.value }">
							<view v-if="form.exerciseHabit === item.value" class="radio-dot"></view>
						</view>
						<text class="radio-label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<!-- 饮食习惯 -->
			<view class="section">
				<text class="section-title">饮食习惯</text>
				<view class="radio-group">
					<view class="radio-item" v-for="item in dietOptions" :key="item.value" @tap="selectDiet(item.value)">
						<view class="radio-circle" :class="{ active: form.dietHabit === item.value }">
							<view v-if="form.dietHabit === item.value" class="radio-dot"></view>
						</view>
						<text class="radio-label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<!-- 睡眠质量 -->
			<view class="section">
				<text class="section-title">睡眠质量</text>
				<view class="radio-group">
					<view class="radio-item" v-for="item in sleepOptions" :key="item.value" @tap="selectSleep(item.value)">
						<view class="radio-circle" :class="{ active: form.sleepQuality === item.value }">
							<view v-if="form.sleepQuality === item.value" class="radio-dot"></view>
						</view>
						<text class="radio-label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<!-- 女性月经史（条件显示） -->
			<view class="section" v-if="showMenstrual">
				<text class="section-title">月经史 <text class="section-sub">（女性）</text></text>
				<textarea class="text-input" v-model="form.menstrualHistory" placeholder="如：初潮13岁，周期28天，经期5天，末次月经2026-06-15" maxlength="500" />
			</view>

			<!-- 其他补充 -->
			<view class="section">
				<text class="section-title">其他补充</text>
				<textarea class="text-input" v-model="form.otherInfo" placeholder="如有其他需要告知的健康信息，请在此填写" maxlength="500" />
			</view>

			<!-- 提交按钮占位 -->
			<view style="height: 160rpx;"></view>
		</scroll-view>

		<view class="bottom-bar">
			<button class="submit-btn" type="default" @tap="handleSubmit" :disabled="submitting">
				<text v-if="!submitting">提交问卷</text>
				<text v-else>提交中...</text>
			</button>
		</view>
	</view>
</template>

<script>
import { questionnaireApi } from '../../common/api.js'

export default {
	data() {
		return {
			submitting: false,
			appointment: null,
			form: {
				familyDiseaseHistory: '',
				pastMedicalHistory: '',
				medicationHistory: '',
				smokingHistory: '',
				drinkingHistory: '',
				exerciseHabit: '',
				dietHabit: '',
				sleepQuality: '',
				menstrualHistory: '',
				allergicHistory: '',
				surgicalHistory: '',
				otherInfo: ''
			},
			smokingOptions: [
				{ label: '从不吸烟', value: 'NEVER' },
				{ label: '已戒烟', value: 'FORMER' },
				{ label: '目前吸烟', value: 'CURRENT' }
			],
			drinkingOptions: [
				{ label: '从不饮酒', value: 'NEVER' },
				{ label: '偶尔饮酒', value: 'OCCASIONAL' },
				{ label: '经常饮酒', value: 'REGULAR' }
			],
			exerciseOptions: [
				{ label: '几乎不运动', value: 'RARELY' },
				{ label: '偶尔运动', value: 'OCCASIONAL' },
				{ label: '规律运动', value: 'REGULAR' },
				{ label: '每天运动', value: 'DAILY' }
			],
			dietOptions: [
				{ label: '清淡饮食', value: '清淡' },
				{ label: '油腻饮食', value: '油腻' },
				{ label: '营养均衡', value: '均衡' }
			],
			sleepOptions: [
				{ label: '良好', value: 'GOOD' },
				{ label: '一般', value: 'FAIR' },
				{ label: '较差', value: 'POOR' }
			],
			questionnaireId: null
		}
	},

	computed: {
		showMenstrual() {
			// 简单判断：从用户信息或档案判断性别，若无则默认显示
			const gender = uni.getStorageSync('userGender')
			return gender === 'FEMALE' || gender === '' || gender == null
		}
	},

	onLoad(options) {
		const apptStr = uni.getStorageSync('selectedAppointment')
		if (apptStr) {
			try {
				this.appointment = typeof apptStr === 'string' ? JSON.parse(apptStr) : apptStr
			} catch (e) {
				this.appointment = apptStr
			}
		}
		// 从 options 也可以获取
		if (options.appointmentId) {
			if (!this.appointment) this.appointment = {}
			this.appointment.id = Number(options.appointmentId)
		}
		this.initQuestionnaire()
	},

	methods: {
		async initQuestionnaire() {
			if (!this.appointment || !this.appointment.id) return

			const userId = uni.getStorageSync('userId')
			try {
				// 尝试获取已有问卷
				const existing = await questionnaireApi.getByAppointment(this.appointment.id)
				if (existing) {
					this.questionnaireId = existing.id
					this.form.familyDiseaseHistory = existing.familyDiseaseHistory || ''
					this.form.pastMedicalHistory = existing.pastMedicalHistory || ''
					this.form.medicationHistory = existing.medicationHistory || ''
					this.form.smokingHistory = existing.smokingHistory || ''
					this.form.drinkingHistory = existing.drinkingHistory || ''
					this.form.exerciseHabit = existing.exerciseHabit || ''
					this.form.dietHabit = existing.dietHabit || ''
					this.form.sleepQuality = existing.sleepQuality || ''
					this.form.menstrualHistory = existing.menstrualHistory || ''
					this.form.allergicHistory = existing.allergicHistory || ''
					this.form.surgicalHistory = existing.surgicalHistory || ''
					this.form.otherInfo = existing.otherInfo || ''
				}
			} catch (e) {
				// 问卷不存在，需要创建
				if (userId) {
					try {
						const created = await questionnaireApi.create(this.appointment.id, Number(userId))
						this.questionnaireId = created.id
					} catch (createErr) {
						console.log('创建问卷失败（可能已存在）', createErr)
					}
				}
			}
		},

		selectSmoking(value) { this.form.smokingHistory = value },
		selectDrinking(value) { this.form.drinkingHistory = value },
		selectExercise(value) { this.form.exerciseHabit = value },
		selectDiet(value) { this.form.dietHabit = value },
		selectSleep(value) { this.form.sleepQuality = value },

		goBack() {
			uni.navigateBack()
		},

		async handleSubmit() {
			if (this.submitting) return

			// 基本验证
			if (!this.questionnaireId) {
				uni.showToast({ title: '问卷初始化中，请稍后', icon: 'none' })
				return
			}

			this.submitting = true
			try {
				await questionnaireApi.update(this.questionnaireId, this.form)
				uni.showToast({ title: '提交成功', icon: 'success' })
				setTimeout(() => {
					uni.navigateBack()
				}, 800)
			} catch (err) {
				let msg = '提交失败'
				if (err && err.message) msg = err.message
				uni.showToast({ title: msg, icon: 'error' })
			} finally {
				this.submitting = false
			}
		}
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f7f7f8; display: flex; flex-direction: column; padding-top: calc(44px); }

.header { display: flex; flex-direction: row; align-items: center; justify-content: space-between; padding: 16rpx 32rpx; background: #ffffff; position: relative; }
.back-btn { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 450; color: #17191c; letter-spacing: -0.25rpx; padding: 12rpx 8rpx; }
.back-btn:active { opacity: 0.6; }
.header-title { font-family: 'Signifier', ui-serif, Georgia, 'Times New Roman', Times, serif; font-size: 40rpx; font-weight: 400; color: #17191c; letter-spacing: -0.3rpx; line-height: 1.1; position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap; }
.header-spacer { width: 80rpx; }

.form-scroll { flex: 1; padding: 24rpx 32rpx 0; }

.tip-card { background: #eef5ff; border-radius: 32rpx; padding: 28rpx; margin-bottom: 24rpx; }
.tip-title { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 500; color: #0057b3; display: block; margin-bottom: 10rpx; }
.tip-text { font-size: 26rpx; color: #4a6fa5; line-height: 1.6; }

.section { background: #ffffff; border-radius: 32rpx; padding: 28rpx 32rpx; margin-bottom: 20rpx; box-shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20rpx 50rpx -10rpx, rgba(0, 0, 0, 0.1) 0px 16rpx 20rpx -12rpx; }
.section-title { font-family: 'Sohne', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 28rpx; font-weight: 500; color: #17191c; display: block; margin-bottom: 20rpx; }
.section-sub { font-size: 24rpx; color: #777b86; }

.text-input { width: 100%; min-height: 140rpx; background: #f7f7f8; border-radius: 24rpx; padding: 20rpx 24rpx; font-size: 26rpx; color: #17191c; border: none; box-sizing: border-box; line-height: 1.6; }

.radio-group { display: flex; flex-direction: row; flex-wrap: wrap; gap: 16rpx; }
.radio-item { display: flex; flex-direction: row; align-items: center; padding: 16rpx 24rpx; background: #f7f7f8; border-radius: 9999px; }
.radio-circle { width: 32rpx; height: 32rpx; border-radius: 50%; border: 2rpx solid #c8cbd0; display: flex; align-items: center; justify-content: center; margin-right: 12rpx; flex-shrink: 0; }
.radio-circle.active { border-color: #0057b3; }
.radio-dot { width: 18rpx; height: 18rpx; border-radius: 50%; background: #0057b3; }
.radio-label { font-size: 26rpx; color: #17191c; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 24rpx 32rpx 40rpx; background: linear-gradient(to top, #ffffff 60%, rgba(255,255,255,0)); pointer-events: none; }
.bottom-bar button { pointer-events: auto; }
.submit-btn { width: 100%; height: 96rpx; line-height: 96rpx; background: #17191c; border-radius: 9999px; font-family: "Sohne", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; font-size: 30rpx; font-weight: 450; color: #ffffff; letter-spacing: -0.27rpx; border: none; box-shadow: none; margin: 0; }
.submit-btn:active { opacity: 0.85; transform: scale(0.97); }
.submit-btn[disabled] { opacity: 0.5; }
</style>
