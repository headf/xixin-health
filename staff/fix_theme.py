# -*- coding: utf-8 -*-
path = r"D:\Desktop\学习课程\Course Design\physical-examination\admin-web\src\app.tsx"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace(
    """          Layout: {
            headerBg: '#FFFFFF',
            siderBg: '#FFFFFF',
            bodyBg: '#F1F5F9',
            headerHeight: 56
          },
          Menu: {
            itemBg: '#FFFFFF',
            itemColor: '#475569',
            itemHoverBg: '#e8f0ff',
            itemHoverColor: '#165DFF',
            itemSelectedBg: '#e8f0ff',
            itemSelectedColor: '#165DFF',
            subMenuItemBg: '#FFFFFF'
          },
          Card: { paddingLG: 24 },
          Table: { headerBg: '#F8FAFC', headerColor: '#64748B' },
          Button: { primaryShadow: 'none' },
          Input: { activeShadow: '0 0 0 3px rgba(22,93,255,0.15)' }""",
    """          Layout: {
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
          Input: { activeShadow: '0 0 0 3px rgba(22,93,255,0.15)' }"""
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
print("ok")
