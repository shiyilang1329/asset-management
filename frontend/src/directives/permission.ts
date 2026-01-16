import type { Directive } from 'vue'
import { useUserStore } from '@/stores/user'

export const permission: Directive = {
  mounted(el: HTMLElement, binding) {
    const { value } = binding
    const userStore = useUserStore()
    
    if (value) {
      const hasPermission = userStore.hasPermission(value)
      if (!hasPermission) {
        el.style.display = 'none'
        // 或者直接移除元素
        // el.parentNode?.removeChild(el)
      }
    }
  }
}
