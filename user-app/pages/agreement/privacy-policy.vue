<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">隐私政策</text>
			<view class="header-spacer"></view>
		</view>
		<view class="update-date">更新日期：2026年7月1日</view>

		<view class="content">
			<view class="section">
				<text class="section-title">一、引言</text>
				<text class="text">熙心健康（以下简称"我们"）深知个人信息对您的重要性。本隐私政策说明在您使用本平台时，我们如何收集、使用、存储和保护您的个人信息。</text>
			</view>

			<view class="section">
				<text class="section-title">二、我们收集的信息</text>
				<text class="text"><text class="bold">注册信息：</text>手机号码、姓名、密码。手机号用于账号注册和身份识别。</text>
				<text class="text"><text class="bold">健康档案：</text>姓名、身份证号、联系电话、既往病史、过敏史、紧急联系人信息等。</text>
				<text class="text"><text class="bold">体检服务：</text>预约信息、体检报告（检验检查结果、医生诊断等）、支付信息等。</text>
				<text class="text"><text class="bold">设备日志：</text>设备型号、IP地址、操作系统、访问记录等。</text>
			</view>

			<view class="section">
				<text class="section-title">三、信息使用</text>
				<text class="text">我们使用您的信息用于提供体检预约、健康档案管理、报告查询等服务，改善产品体验，提供个性化健康管理建议。</text>
			</view>

			<view class="section">
				<text class="section-title">四、信息存储与保护</text>
				<text class="text">您的个人信息存储于中华人民共和国境内服务器。我们采用SSL/TLS加密传输、数据加密存储、访问权限控制等安全措施保护您的数据。医疗数据依法保存不少于15年。</text>
			</view>

			<view class="section">
				<text class="section-title">五、信息共享</text>
				<text class="text">我们仅与您选择的体检机构共享必要信息以提供服务。我们不会出售您的个人信息。如涉及业务合并等交易，将提前告知。</text>
			</view>

			<view class="section">
				<text class="section-title">六、您的权利</text>
				<text class="text">您有权查询、更正、删除您的个人信息，有权撤回同意，有权申请注销账号。</text>
			</view>

			<view class="section">
				<text class="section-title">七、未成年人保护</text>
				<text class="text">若您是18周岁以下的未成年人，在使用本平台前应事先取得法定监护人的同意。</text>
			</view>

			<view class="section">
				<text class="section-title">八、联系我们</text>
				<text class="text">邮箱：support@xixin-health.com\n数据保护：privacy@xixin-health.com\n电话：400-xxx-xxxx</text>
				<text class="text">我们将在15个工作日内回复您的隐私请求。</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	methods: {
		goBack() { uni.navigateBack() }
	}
}
</script>

<style>
.page { min-height: 100vh; background: #f7f7f8; padding-top: calc(44px); }
.header {
	display: flex; align-items: center; justify-content: space-between;
	padding: 16rpx 32rpx; background: #ffffff; position: relative;
}
.back-btn { font-size: 28rpx; font-weight: 450; color: #17191c; padding: 12rpx 8rpx; }
.header-title {
	font-size: 36rpx; font-weight: 500; color: #17191c;
	position: absolute; left: 50%; transform: translateX(-50%); white-space: nowrap;
}
.header-spacer { width: 80rpx; }
.update-date { text-align: center; font-size: 24rpx; color: #94a3b8; padding: 16rpx 0; background: #fff; }
.content { padding: 24rpx 32rpx 60rpx; }
.section { background: #ffffff; border-radius: 24rpx; padding: 28rpx 24rpx; margin-bottom: 20rpx; }
.section-title { font-size: 30rpx; font-weight: 600; color: #17191c; display: block; margin-bottom: 16rpx; }
.text { font-size: 26rpx; line-height: 1.7; color: #334155; display: block; margin-bottom: 12rpx; }
.bold { font-weight: 600; }
</style>
