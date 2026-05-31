/**
 * 考试防作弊 composable
 * 集中管理禁止复制、切屏上报等监听器
 */
import { examCheat } from '@/api/exam'

/**
 * @param {Function|import('vue').Ref} examIdRef 考试 ID 获取函数或 Ref
 * @param {Function} [onCheatResponse] 切屏上报回调
 */
export function useExamAntiCheat(examIdRef, onCheatResponse) {
  const handlers = {}

  const disableContextMenu = (e) => e.preventDefault()
  const disableCopy = (e) => e.preventDefault()
  const disableCut = (e) => e.preventDefault()
  const disablePaste = (e) => e.preventDefault()
  const disableSelect = (e) => e.preventDefault()
  const disablePrintScreen = (e) => {
    if (e.keyCode === 44) e.preventDefault()
  }
  const disableDevTools = (e) => {
    if (
      (e.ctrlKey && e.shiftKey && e.keyCode === 73) ||
      (e.ctrlKey && e.keyCode === 85) ||
      e.keyCode === 123
    ) {
      e.preventDefault()
    }
  }

  const pageHidden = () => {
    const examId = typeof examIdRef === 'function' ? examIdRef() : (examIdRef?.value ?? examIdRef)
    if (document.visibilityState === 'hidden' && examId) {
      examCheat(examId)
        .then((res) => {
          if (typeof onCheatResponse === 'function') {
            onCheatResponse(res)
          }
        })
        .catch(() => {})
    }
  }

  function enableAntiCheat() {
    document.addEventListener('contextmenu', disableContextMenu)
    document.addEventListener('copy', disableCopy)
    document.addEventListener('cut', disableCut)
    document.addEventListener('paste', disablePaste)
    document.addEventListener('selectstart', disableSelect)
    document.addEventListener('keydown', disablePrintScreen)
    document.addEventListener('keydown', disableDevTools)
    document.addEventListener('visibilitychange', pageHidden)
    handlers.active = true
  }

  function disableAntiCheat() {
    document.removeEventListener('contextmenu', disableContextMenu)
    document.removeEventListener('copy', disableCopy)
    document.removeEventListener('cut', disableCut)
    document.removeEventListener('paste', disablePaste)
    document.removeEventListener('selectstart', disableSelect)
    document.removeEventListener('keydown', disablePrintScreen)
    document.removeEventListener('keydown', disableDevTools)
    document.removeEventListener('visibilitychange', pageHidden)
    handlers.active = false
  }

  return { enableAntiCheat, disableAntiCheat }
}
