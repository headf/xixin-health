<template>
	<view class="page">
		<view class="header">
			<text class="back-btn" @tap="goBack">← 返回</text>
			<text class="header-title">用户协议</text>
			<view class="header-spacer"></view>
		</view>
		<view class="update-date">更新日期：2026年7月1日</view>

		<view class="content">
			<view class="section">
				<text class="section-title">一、导言</text>
				<text class="text">欢迎您使用熙心健康体检管理系统（以下简称"本平台"或"熙心健康"）。本平台由熙心健康科技运营，为用户提供体检预约、健康档案管理、体检报告查询、健康评估等健康管理服务。</text>
				<text class="text">请您在注册、使用本平台服务前，仔细阅读并充分理解本协议的全部内容。您通过注册、登录、使用本平台服务，即表示您已阅读并同意受本协议的约束。</text>
			</view>

			<view class="section">
				<text class="section-title">二、账号注册与管理</text>
				<text class="text"><text class="bold">2.1 注册资格</text> 您应当是具备完全民事权利能力和完全民事行为能力的自然人。</text>
				<text class="text"><text class="bold">2.2 账号注册</text> 您须提供真实、准确、完整的个人信息，包括手机号码、身份证号、姓名等。</text>
				<text class="text"><text class="bold">2.3 账号安全</text> 您应妥善保管账号及密码，对使用该账号的一切活动负完全责任。</text>
				<text class="text"><text class="bold">2.4 账号注销</text> 您有权申请注销账号。</text>
			</view>

			<view class="section">
				<text class="section-title">三、服务内容</text>
				<text class="text">本平台提供健康档案管理、体检预约服务、体检报告查询、健康评估与干预等服务。健康评估结果仅供参考，不能替代专业医疗诊断。</text>
			</view>

			<view class="section">
				<text class="section-title">四、用户权利与义务</text>
				<text class="text">您应保证信息真实性，合法使用本平台，不得转借账号。您有权查看、导出、更正、删除您的个人信息。</text>
			</view>

			<view class="section">
				<text class="section-title">五、平台权利与义务</text>
				<text class="text">本平台将尽力保障服务稳定安全运行，高度重视用户隐私保护，并有权根据需要修改本协议条款。</text>
			</view>

			<view class="section">
				<text class="section-title">六、知识产权与免责</text>
				<text class="text">本平台所含全部知识产权归熙心健康科技所有。本平台提供的健康建议仅供参考，不构成医疗诊断意见。</text>
			</view>

			<view class="section">
				<text class="section-title">七、争议解决</text>
				<text class="text">本协议适用中华人民共和国法律。争议经协商不成的，可向本平台运营方所在地有管辖权的人民法院提起诉讼。</text>
			</view>

			<view class="section">
				<text class="section-title">八、联系方式</text>
				<text class="text">客服邮箱：support@xixin-health.com\n联系电话：400-xxx-xxxx</text>
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
