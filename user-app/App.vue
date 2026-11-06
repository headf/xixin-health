<script>
	export default {
		onLaunch: function() {
			console.log('熙心健康 App 启动')

			// 获取系统状态栏高度并设置为 CSS 变量
			try {
				const sys = uni.getSystemInfoSync?.() || uni.getWindowInfo?.() || {}
				const h = sys.statusBarHeight || 44
				document.documentElement.style.setProperty('--status-bar-height', h + 'px')
				document.documentElement.style.setProperty('--window-width', sys.windowWidth + 'px')
				console.log('状态栏高度:', h)
			} catch(e) {}

			// 设置状态栏文字为深色（适配浅色背景）
			// #ifdef APP-PLUS
			plus.navigator.setStatusBarStyle('dark')
			// #endif

			// 检查登录状态
			const token = uni.getStorageSync('accessToken')
			if (token) {
				console.log('用户已登录')
			} else {
				console.log('用户未登录')
			}
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/* 熙心健康 App — 全局基础样式 */
	page {
		background-color: var(--surface-page, #f8fafc);
		font-family: var(--font, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif);
		box-sizing: border-box;
		color: var(--text, #1e293b);
		-webkit-font-smoothing: antialiased;
		-moz-osx-font-smoothing: grayscale;
		font-size: 28rpx;
		line-height: 1.5;
	}

	/* 重置 uni-app button 默认样式 */
	button {
		padding: 0;
		margin: 0;
		background: transparent;
		border: none;
		border-radius: 0;
		line-height: inherit;
		font-size: inherit;
		color: inherit;
	}
	button::after {
		border: none;
	}

	input, textarea {
		outline: none;
		border: none;
		background: transparent;
	}

	view, scroll-view, text, image, button, input, textarea {
		-webkit-tap-highlight-color: transparent;
	}

	view, scroll-view {
		box-sizing: border-box;
	}

	image {
		display: block;
	}

	/* 全局工具类 */
	.ellipsis {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
</style>
