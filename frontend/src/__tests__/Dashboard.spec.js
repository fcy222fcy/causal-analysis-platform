import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Dashboard from '../views/Dashboard.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/dashboard', component: Dashboard }
  ]
})

describe('Dashboard.vue', () => {
  it('renders dashboard with stats', () => {
    const wrapper = mount(Dashboard, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('.stat-card').exists()).toBe(true)
  })

  it('displays sensor count', () => {
    const wrapper = mount(Dashboard, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.text()).toContain('传感器数量')
  })

  it('displays anomaly count', () => {
    const wrapper = mount(Dashboard, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.text()).toContain('异常事件')
  })
})
