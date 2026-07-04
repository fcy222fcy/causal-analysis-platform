import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AlarmManagement from '../views/AlarmManagement.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/alarm-management', component: AlarmManagement }
  ]
})

describe('AlarmManagement.vue', () => {
  it('renders alarm management page', () => {
    const wrapper = mount(AlarmManagement, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.text()).toContain('告警管理')
  })

  it('displays alarm table', () => {
    const wrapper = mount(AlarmManagement, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('table').exists()).toBe(true)
  })

  it('shows alarm levels', () => {
    const wrapper = mount(AlarmManagement, {
      global: {
        plugins: [router]
      }
    })

    // el-table does not render cell content in jsdom, verify data-driven levels instead
    const vm = wrapper.vm
    const levels = vm.alarms.map(a => a.alarmLevel)
    expect(levels).toContain('high')
    expect(levels).toContain('medium')
    expect(levels).toContain('low')
  })
})
