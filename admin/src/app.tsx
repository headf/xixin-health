import { AuditOutlined, BarChartOutlined, BellOutlined, DownOutlined, FileDoneOutlined, InfoCircleOutlined, MedicineBoxOutlined, MessageOutlined, OrderedListOutlined, SafetyCertificateOutlined, SearchOutlined, SettingOutlined, TeamOutlined } from '@ant-design/icons';
import { ProLayout } from '@ant-design/pro-components';
import { Avatar, Badge, Button, ConfigProvider, Dropdown, Input, Layout, message, Space, Tag } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { useMemo, useState } from 'react';
import LoginPage from './pages/LoginPage';
import AuditLogPage from './pages/audit/AuditLogPage';
import BranchPage from './pages/config/BranchPage';
import DepartmentPage from './pages/config/DepartmentPage';
import ExamItemPage from './pages/config/ExamItemPage';
import FlowNodePage from './pages/config/FlowNodePage';
import PackagePage from './pages/config/PackagePage';
import RulePage from './pages/config/RulePage';
import CheckInPage from './pages/exam/CheckInPage';
import DepartmentQueuePage from './pages/exam/DepartmentQueuePage';
import GuideSheetPage from './pages/exam/GuideSheetPage';
import ProgressPage from './pages/exam/ProgressPage';
import AccountPage from './pages/iam/AccountPage';
import RolePage from './pages/iam/RolePage';
import InterventionPage from './pages/management/InterventionPage';
import RiskUserPage from './pages/management/RiskUserPage';
import OverviewPage from './pages/OverviewPage';
import ReportWarningPage from './pages/report/ReportWarningPage';
import TemplatePage from './pages/report/TemplatePage';
import BusinessStatisticsPage from './pages/statistics/BusinessStatisticsPage';
import ExportPage from './pages/statistics/ExportPage';
import HealthStatisticsPage from './pages/statistics/HealthStatisticsPage';
import { clearAdminToken, getAdminToken } from './services/request';
import AboutPage from './pages/AboutPage';
import ContactPage from './pages/ContactPage';
import ContentEditPage from './pages/ContentEditPage';
import ContactMessagesPage from './pages/ContactMessagesPage';

const pageMap: Record<string, JSX.Element> = {
  '/overview': <OverviewPage />,
  '/config/exam-items': <ExamItemPage />,
  '/config/packages': <PackagePage />,
  '/config/branches': <BranchPage />,
  '/config/departments': <DepartmentPage />,
  '/config/rules': <RulePage />,
  '/config/flow-nodes': <FlowNodePage />,
  '/iam/roles': <RolePage />,
  '/iam/accounts': <AccountPage />,
  '/exam/check-in': <CheckInPage />,
  '/exam/guide-sheet': <GuideSheetPage />,
  '/exam/queues': <DepartmentQueuePage />,
  '/exam/progress': <ProgressPage />,
  '/report/templates': <TemplatePage />,
  '/report/warnings': <ReportWarningPage />,
  '/management/risk-users': <RiskUserPage />,
  '/management/intervention': <InterventionPage />,
  '/statistics/business': <BusinessStatisticsPage />,
  '/statistics/health': <HealthStatisticsPage />,
  '/statistics/export': <ExportPage />,
  '/audit/logs': <AuditLogPage />,
  '/about': <AboutPage />,
  '/contact': <ContactPage />,
  '/system/content-edit': <ContentEditPage />,
  '/system/contact-messages': <ContactMessagesPage />,
};

export default function App() {
  const [pathname, setPathname] = useState('/overview');
  const [collapsed, setCollapsed] = useState(false);
  const [token, setToken] = useState(getAdminToken());

  const route = useMemo(() => ({
    path: '/',
    routes: [
      { path: '/overview', name: '总览', icon: <BarChartOutlined /> },
      {
        name: '基础配置', path: '/config', icon: <SettingOutlined />,
        routes: [
          { path: '/config/exam-items', name: '体检项目' }, { path: '/config/packages', name: '套餐配置' },
          { path: '/config/branches', name: '分院管理' }, { path: '/config/departments', name: '科室管理' },
          { path: '/config/rules', name: '规则配置' }, { path: '/config/flow-nodes', name: '流程节点' }
        ]
      },
      {
        name: '权限与账号', path: '/iam', icon: <SafetyCertificateOutlined />,
        routes: [{ path: '/iam/roles', name: '角色管理' }, { path: '/iam/accounts', name: '账号管理' }]
      },
      {
        name: '体检过程', path: '/exam', icon: <MedicineBoxOutlined />,
        routes: [
          { path: '/exam/check-in', name: '签到登记' }, { path: '/exam/guide-sheet', name: '导检单' },
          { path: '/exam/queues', name: '科室队列' }, { path: '/exam/progress', name: '进度跟踪' }
        ]
      },
      {
        name: '报告管理', path: '/report', icon: <OrderedListOutlined />,
        routes: [{ path: '/report/templates', name: '报告模板' }, { path: '/report/warnings', name: '异常预警' }]
      },
      {
        name: '健康干预', path: '/management', icon: <TeamOutlined />,
        routes: [{ path: '/management/risk-users', name: '高危人群' }, { path: '/management/intervention', name: '随访干预' }]
      },
      {
        name: '数据统计', path: '/statistics', icon: <BarChartOutlined />,
        routes: [{ path: '/statistics/business', name: '业务统计' }, { path: '/statistics/health', name: '健康统计' }, { path: '/statistics/export', name: '报表导出' }]
      },
      { name: '操作审计', path: '/audit', icon: <AuditOutlined />, routes: [{ path: '/audit/logs', name: '操作日志' }] },
      { name: '系统管理', path: '/system', icon: <SettingOutlined />, routes: [{ path: '/system/content-edit', name: '内容编辑' }, { path: '/system/contact-messages', name: '客服留言' }] }
    ]
  }), []);

  const userMenu = {
    items: [
      { key: 'profile', label: '个人中心' },
      { key: 'logout', label: '退出登录', danger: true }
    ],
    onClick: ({ key }: { key: string }) => {
      if (key === 'logout') { clearAdminToken(); setToken(null); message.success('已退出'); }
    }
  };

  if (!token) {
    return (
      <ConfigProvider locale={zhCN} theme={{
        token: { colorPrimary: '#0ea5e9', borderRadius: 16, fontFamily: '-apple-system, "PingFang SC", "Microsoft YaHei", sans-serif' }
      }}>
        <LoginPage onLogin={() => setToken(getAdminToken())} />
      </ConfigProvider>
    );
  }

  return (
    <ConfigProvider locale={zhCN} theme={{
      token: {
        colorPrimary: '#0ea5e9', borderRadius: 16,
        fontFamily: '-apple-system, "PingFang SC", "Microsoft YaHei", sans-serif',
        colorBgLayout: '#f8fafc', colorBgContainer: '#ffffff', colorBorder: '#e2e8f0',
        colorText: '#0f172a', colorTextSecondary: '#94a3b8',
        boxShadow: '0 4px 20px rgba(0,0,0,0.04)'
      },
      components: {
        Layout: { siderBg: '#ffffff', bodyBg: '#f8fafc' },
        Menu: {
          itemBg: '#ffffff', itemColor: '#475569', itemHoverBg: '#f8fafc', itemHoverColor: '#0ea5e9',
          itemSelectedBg: '#e0f2fe', itemSelectedColor: '#0ea5e9', subMenuItemBg: '#ffffff',
          activeBarWidth: 0, itemMarginBlock: 2, itemHeight: 40
        },
        Card: { paddingLG: 24 },
        Table: { headerBg: '#f8fafc', headerColor: '#94a3b8' },
        Button: { primaryShadow: 'none', borderRadius: 16, controlHeight: 38 },
        Input: { activeShadow: '0 0 0 3px rgba(14,165,233,0.1)', borderRadius: 16 },
        Tag: { borderRadius: 9999 }
      }
    }}>
      <ProLayout
        className="admin-shell"
        title="熙心健康"
        logo={<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#0ea5e9" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2L2 7v10l10 5 10-5V7L12 2z"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/>
        </svg>}
        route={route}
        location={{ pathname }}
        layout="side"
        siderWidth={220}
        collapsedWidth={60}
        collapsed={collapsed}
        onCollapse={setCollapsed}
        pageTitleRender={false}
        contentStyle={{ padding: 24 }}
        headerRender={false}
        menuItemRender={(item, dom) => (
          <a onClick={() => { item.path && setPathname(item.path); if (collapsed) setCollapsed(false); }}
             style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '8px 16px' }}>
            {item.icon ? <span style={{ fontSize: 18 }}>{item.icon}</span> : null}
            {!collapsed && <span style={{ fontSize: 14 }}>{item.name}</span>}
          </a>
        )}
        actionsRender={() => [
          <Dropdown key="user" menu={userMenu} placement="bottomRight">
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', padding: '4px 8px 4px 4px', borderRadius: 16, background: '#f8fafc', marginRight: 8 }}>
              <Avatar size={28} style={{ background: '#e0f2fe', color: '#0ea5e9', fontWeight: 700, fontSize: 12 }}>管</Avatar>
              <div style={{ lineHeight: 1.2, textAlign: 'left' }}>
                <div style={{ fontSize: 12, fontWeight: 600, color: '#0f172a' }}>管理员</div>
                <div style={{ fontSize: 9, color: '#22c55e', display: 'flex', alignItems: 'center', gap: 2 }}>
                  <span style={{ width: 5, height: 5, borderRadius: '50%', background: '#22c55e', display: 'inline-block' }} />在线
                </div>
              </div>
              <DownOutlined style={{ fontSize: 9, color: '#94a3b8' }} />
            </div>
          </Dropdown>
        ]}
      >
        {pageMap[pathname] || <OverviewPage />}
      </ProLayout>
    </ConfigProvider>
  );
}
