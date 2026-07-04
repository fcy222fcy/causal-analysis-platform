import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import CausalAnalysis from '../views/CausalAnalysis.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/causal-analysis', component: CausalAnalysis }
  ]
})

describe('CausalAnalysis.vue', () => {
  it('renders causal analysis page', () => {
    const wrapper = mount(CausalAnalysis, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.text()).toContain('因果关系图')
    expect(wrapper.text()).toContain('因果路径')
  })

  it('displays causal paths table', () => {
    const wrapper = mount(CausalAnalysis, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('table').exists()).toBe(true)
  })

  it('has analyze button', () => {
    const wrapper = mount(CausalAnalysis, {
      global: {
        plugins: [router]
      }
    })

    const button = wrapper.find('button')
    expect(button.exists()).toBe(true)
    expect(button.text()).toContain('执行分析')
  })
})
