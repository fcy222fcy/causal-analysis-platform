import { config } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { vi } from 'vitest'

// Mock echarts since jsdom doesn't support canvas
vi.mock('echarts', () => {
  const mockChart = {
    setOption: vi.fn(),
    resize: vi.fn(),
    dispose: vi.fn(),
    on: vi.fn(),
    off: vi.fn()
  }
  return {
    default: {
      init: vi.fn(() => mockChart),
      getInstanceByDom: vi.fn(),
      dispose: vi.fn()
    },
    init: vi.fn(() => mockChart)
  }
})

config.global.plugins = [ElementPlus]
