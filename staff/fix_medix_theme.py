# -*- coding: utf-8 -*-
path = r"D:\Desktop\学习课程\Course Design\physical-examination\admin-web\src\app.tsx"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Replace the ConfigProvider theme for logged-in state
old_theme = """          colorPrimary: '#165DFF',
          borderRadius: 8,
          fontFamily: '"PingFang SC", "Microsoft YaHei", "Noto Sans SC", -apple-system, BlinkMacSystemFont, sans-serif',
          colorBgLayout: '#F1F5F9',
          colorBgContainer: '#FFFFFF',
          colorBorder: '#E2E8F0',
          colorText: '#1E293B',
          colorTextSecondary: '#64748B',
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)'
        },
        components: {
          Layout: {
            siderBg: '#FFFFFF',
            bodyBg: '#F1F5F9'
          },
          Menu: {
            itemBg: '#FFFFFF',
            itemColor: '#475569',
            itemHoverBg: '#F1F5F9',
            itemHoverColor: '#334155',
            itemSelectedBg: '#165DFF',
            itemSelectedColor: '#FFFFFF',
            subMenuItemBg: '#FFFFFF'
          },
          Card: { paddingLG: 24 },
          Table: { headerBg: '#F8FAFC', headerColor: '#64748B' },
          Button: { primaryShadow: 'none' },
          Input: { activeShadow: '0 0 0 3px rgba(22,93,255,0.15)' }
        }"""

new_theme = """          colorPrimary: '#0EA5E9',
          borderRadius: 10,
          fontFamily: '"Inter", "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, sans-serif',
          colorBgLayout: '#f0f4f8',
          colorBgContainer: '#FFFFFF',
          colorBorder: '#e2e8f0',
          colorText: '#1e293b',
          colorTextSecondary: '#64748b',
          colorBgElevated: '#FFFFFF',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)'
        },
        components: {
          Layout: {
            siderBg: '#1e293b',
            bodyBg: '#f0f4f8'
          },
          Menu: {
            itemBg: 'transparent',
            itemColor: '#94a3b8',
            itemHoverBg: 'rgba(255,255,255,0.08)',
            itemHoverColor: '#FFFFFF',
            itemSelectedBg: 'rgba(14,165,233,0.2)',
            itemSelectedColor: '#FFFFFF',
            subMenuItemBg: 'transparent',
            iconSize: 18,
            itemMarginInline: 12,
            itemBorderRadius: 8
          },
          Card: {
            paddingLG: 20
          },
          Table: {
            headerBg: '#f8fafc',
            headerColor: '#64748b',
            rowHoverBg: '#f8fafc'
          },
          Button: {
            primaryShadow: 'none',
            borderRadius: 6
          },
          Input: {
            activeShadow: '0 0 0 3px rgba(14,165,233,0.15)',
            borderRadius: 8
          },
          Statistic: {
            contentFontSize: 26,
            titleFontSize: 13
          },
          Tag: {
            borderRadius: 9999
          }
        }"""

c = c.replace(old_theme, new_theme)

# Replace the ProLayout title/logo to match dark sidebar
c = c.replace("""      <ProLayout
        className="admin-shell"
        title="熙心健康"
        logo={<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#165DFF" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
        </svg>}
        route={route}
        location={{ pathname }}
        layout="side"
        siderWidth={240}
        collapsedWidth={64}
        collapsed={collapsed}
        onCollapse={setCollapsed}
        contentStyle={{ padding: 24 }}
        headerRender={false}""",
"""      <ProLayout
        className="admin-shell"
        title="熙心健康"
        logo={<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
        </svg>}
        route={route}
        location={{ pathname }}
        layout="side"
        siderWidth={240}
        collapsedWidth={64}
        collapsed={collapsed}
        onCollapse={setCollapsed}
        contentStyle={{ padding: 24 }}
        headerRender={false}
        siderMenuType="group"
        menuProps={{ forceSubMenuRender: true, inlineIndent: 16 }}""")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("done")
