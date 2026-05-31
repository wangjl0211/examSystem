import { createRouter, createWebHashHistory } from 'vue-router'

/**
 * 路由配置文件
 * 定义应用的所有路由，包括常量路由和权限路由
 */

/* 布局组件 */
import Layout from '@/layout/index.vue'

/**
 * 注意：子菜单仅在路由的children.length >= 1时才会显示
 * 详见：https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   如果设置为true，该项不会显示在侧边栏中（默认为false）
 * alwaysShow: true               如果设置为true，将始终显示根菜单  
 * redirect: noRedirect           如果设置为noRedirect，面包屑中不会进行重定向
 * name:'router-name'             该名称用于<keep-alive>（必须设置！！！）
 * meta : {
 *    roles: ['admin','teacher','student']    控制页面角色权限（可以设置多个角色）
 *    title: 'title'               侧边栏和面包屑中显示的名称（建议设置）
 *    icon: 'svg-name'/'el-icon-x' 侧边栏中显示的图标
 *    breadcrumb: false            如果设置为false，该项将在面包屑中隐藏（默认为true）
 *    activeMenu: '/example/list'  如果设置了路径，侧边栏将高亮你设置的路径
 * }
 */

/**
 * 常量路由
 * 不需要权限控制的基础页面
 * 所有角色都可以访问
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/admin/login',
    component: () => import('@/views/login/adminLogin.vue'),
    hidden: true,
    meta: { requiresAdminIP: true }
  },

  {
    path: '/404',
    component: () => import('@/views/404.vue'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/login/register.vue'),
    hidden: true
  },

  {
    path: '/forgot-password',
    component: () => import('@/views/login/forgotPassword.vue'),
    hidden: true
  },

  {
    path: '/admin/forgot-password',
    component: () => import('@/views/login/adminForgotPassword.vue'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    meta: { 
      requireAuth: true ,
      roles: ['admin', 'teacher', 'student']
    },
    redirect: '/index', // 添加重定向规则
    children: [{
      path: 'index',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index.vue'),
      meta: { 
        title: '主页', 
        icon: 'el-icon-s-home', 
        visible: true,
        roles: ['admin', 'teacher', 'student']
      }
    }]
  },

  {
    path: '/user-management',
    component: Layout,
    children: [{
      path: '/user-management',
      name: 'user-management',
      component: () => import('@/views/user/index.vue'),
      meta: { title: '用户管理', icon: 'el-icon-user', visible: true, roles: ['admin'] }
    }]
  },
  {
    path: '/myself',
    component: Layout,
    children: [{
      path: '/myself',
      name: 'myself',
      hidden: true,
      component: () => import('@/views/user/myself.vue'),
      meta: { title: '个人中心', visible: true, roles: ['admin', 'teacher', 'student'], icon: 'dashboard' }
    }]
  },
  {
    path: '/change-password',
    component: Layout,
    children: [{
      path: '/change-password',
      name: 'change-password',
      hidden: true,
      component: () => import('@/views/user/updatePassword.vue'),
      meta: { title: '修改密码', visible: true, roles: ['admin', 'teacher', 'student'], icon: 'dashboard' }
    }]
  },
  {
    path: '/class-management',
    component: Layout,
    children: [{
      path: '/class-management',
      name: 'class-management',
      component: () => import('@/views/class/index.vue'),
      meta: { title: '课程管理', visible: true, roles: ['admin', 'teacher', 'student'], icon: 'el-icon-takeaway-box' }
    }]
  },
  {
    path: '/class-detail',
    component: Layout,
    children: [{
      path: '/class-detail',
      name: 'class-detail',
      hidden: true,
      component: () => import('@/views/class/detail.vue'),
      meta: { title: '课程详情', visible: true, roles: ['admin', 'teacher'], icon: 'el-icon-takeaway-box' }
    }]
  },
  {
    path: '/discussion-management',
    component: Layout,
    children: [{
      path: 'discussion-management',
      name: 'discussion-management',
      component: () => import('@/views/discuss/index.vue'),
      meta: { title: '讨论管理', visible: true, roles: ['teacher', 'student'], icon: 'el-icon-chat-dot-square' },
  }],
  },
  {
    path: '/discussion-detail',
    component: Layout,
    children: [{
      path: 'discussion-detail',
      hidden: true,
      name: 'discussion-detail',
      component: () => import('@/views/discuss/detail.vue'),
      meta: { title: '讨论详情', visible: true, roles:['teacher', 'student'], icon: 'el-icon-takeaway-box' },
  }],
},
{
  path: '/exam-details',
  component: Layout,
  children: [{
    path: 'exam-details',
    hidden: true,
    name: 'exam-details',
    component: () => import('@/views/exam/details.vue'),
    meta: { title: '考试详情', visible: true, roles:['teacher'], icon: 'el-icon-takeaway-box' },
}],
},
{
  path: '/discussion-block',
  component: Layout,
  children: [
    {
      path: 'discussion-block',
      name: 'discussion-block',
      hidden: true,
      component: () => import('@/views/discuss/block.vue'),
      meta: {
        title: '投屏模式',
        visible: false,
        roles: ['teacher'],
      },
    },
  ],
},
  {
    path: '/text-center',
    component: Layout,
    children: [{
      path: '/text-center',
      name: 'text-center',
      component: () => import('@/views/exam/student/index.vue'),
      meta: { title: '试卷中心', visible: true, roles: ['student'], icon: 'el-icon-document-copy' }
    }]
  },
  {
    path: '/start-exam/:id?',
    name: 'start-exam',
    hidden: true,
    component: () => import('@/views/exam/index.vue'),
    meta: { title: '开始考试', visible: true, roles: ['teacher', 'student'], icon: 'dashboard' }
  },
  {
    path: '/exercise-center',
    component: Layout,
    children: [{
      path: '/exercise-center',
      name: 'exercise-center',
      component: () => import('@/views/exercise/index.vue'),
      // 'admin',
      meta: { title: '刷题中心', visible: true, roles: ['student'], icon: 'el-icon-tickets' }
    }]
  },
  {
    path: '/start-exercise',
    name: 'start-exercise',
    hidden: true,
    component: () => import('@/views/exercise/exercise.vue'),
    meta: { title: '开始刷题', visible: true, roles: ['teacher', 'student'], icon: 'dashboard' }

  },


  {
    path: '/exam-record-detail',
    component: Layout,
    children: [{
      path: '/exam-record-detail',
      name: 'exam-record-detail',
      hidden: true,
      component: () => import('@/views/record/exam/newk.vue'),
      meta: { title: '考试记录查看', visible: true, roles: ['teacher', 'student'], icon: 'dashboard' }
    }]
  },


  {
    path: '/exam-management',
    component: Layout,
    children: [{
      path: '/exam-management',
      name: 'exam-management',
      component: () => import('@/views/exam/teacher/index.vue'),
      // , 'admin'
      meta: { title: '考试管理', visible: true, roles: ['teacher'], icon: 'el-icon-document' }
    }]
  },
  {
    path: '/exam-add',
    component: Layout,
    children: [{
      path: '/exam-add',
      name: 'exam-add',
      hidden: true,
      component: () => import('@/views/exam/examAdd.vue'),
      meta: { title: '考试添加', visible: true, roles: ['teacher'], icon: 'dashboard' }
    }]
  },
  {
    path: '/repo-management',
    component: Layout,
    children: [{
      path: '/repo-management',
      name: 'repo-management',
      component: () => import('@/views/repo/index.vue'),
      meta: { title: '题库管理', visible: true, roles: ['teacher'], icon: 'el-icon-folder-opened' }
    }]
  },
  {
    path: '/questions-management',
    component: Layout,
    children: [{
      path: '/questions-management',
      name: 'questions-management',
      component: () => import('@/views/question/index.vue'),
      meta: { title: '试题管理', visible: true, roles: ['teacher'], icon: 'el-icon-document-copy' }
    }]
  },
  {
    path: '/questions-add',
    component: Layout,
    children: [{
      path: '/questions-add',
      name: 'questions-add',
      hidden: true,
      component: () => import('@/views/question/add.vue'),
      meta: { title: '试题添加', visible: true, roles: ['teacher'], icon: 'dashboard' }
    }]
  },

  {
    path: '/score-analysis',
    component: Layout,
    children: [{
      path: 'score-analysis',
      name: 'score-analysis',
      component: () => import('@/views/score/index.vue'),
      // , 'admin'
      meta: { title: '成绩分析', visible: true, roles: ['teacher'], icon: 'el-icon-pie-chart' }
    }]
  },
  {
    path: '/detail',
    component: Layout,
    children: [{
      path: '/user-score',
      name: 'user-score',
      hidden: true,
      component: () => import('@/views/score/detail.vue'),
      meta: { title: '用户成绩', visible: true, roles: ['teacher'], icon: 'dashboard' }
    }]
  },
  {
    path: '/answer-manage',
    component: Layout,
    children: [{
      path: 'marking-management',
      name: 'marking-management',
      component: () => import('@/views/answer/index.vue'),
      // , 'admin'
      meta: { title: '阅卷管理', visible: true, roles: ['teacher'], icon: 'el-icon-files' }
    }]
  },
  {
    path: '/answer',
    component: Layout,
    children: [{
      path: '/answer-show',
      name: 'answer-show',
      hidden: true,
      component: () => import('@/views/answer/answerck.vue'),
      meta: { title: '答卷查看', visible: true, roles: ['teacher', 'admin'], icon: 'dashboard' }
    }]
  },
  {
    path: '/makeTest',
    component: Layout,
    children: [{
      path: '/makeTest',
      name: 'makeTest',
      hidden: true,
      component: () => import('@/views/answer/makeTest.vue'),
      meta: { title: '批改试卷', visible: true, roles: ['teacher'], icon: 'dashboard' }
    }]
  },

  {
    path: '/notice-management',
    component: Layout,
    children: [{
      path: '/notice-management',
      name: 'notice-management',
      component: () => import('@/views/notice/notice.vue'),
      meta: { title: '公告管理', visible: true, roles: ['admin'], icon: 'el-icon-bell' }
    }]
  },

  {
    path: '/api-document',
    component: Layout,
    children: [{
      path: '/api-document',
      name: 'ApiDocument',
      component: () => import('@/views/document/ApiDocument.vue'),
      meta: { title: '系统接口', visible: true, roles: ['admin'], icon: 'el-icon-connection' }
    }]
  },
  {
    path: '/ip-whitelist',
    component: Layout,
    children: [{
      path: '/ip-whitelist',
      name: 'ip-whitelist',
      component: () => import('@/views/admin/ipWhitelist.vue'),
      meta: { title: 'IP白名单', visible: true, roles: ['admin'], icon: 'el-icon-lock' }
    }]
  },
  {
    path: "/login-log",
    component: Layout,
    meta: { 
      title: "登录日志", 
      visible: true,
      roles: ["admin", "teacher", "student"],
      icon: "el-icon-receiving"
    },
    children: [
      {
        path: "/login-log",
        name: "login-log",
        component: () => import("@/views/log/index.vue"),
        meta: {
          title: "登录日志",
          visible: true,
          roles: ["admin", "teacher", "student"],
          icon: "el-icon-receiving",
        },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/404', hidden: true }
]

const router = createRouter({
  history: createWebHashHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: constantRoutes
})

// 详见：https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  console.warn('resetRouter在Vue Router 4迁移中尚未完全支持。')
}

export default router
