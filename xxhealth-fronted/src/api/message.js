import request from './request'

/**
 * 消息列表（type / keyword / status 可选）
 */
export function listMessages(query) {
  return request.post('/message/list', query || {})
}

/**
 * 单条标记已读
 */
export function markMessageRead(msgid) {
  return request.post('/message/markRead', { msgid })
}

/**
 * 全部标记已读
 */
export function markAllMessagesRead() {
  return request.post('/message/markAllRead', {})
}

/**
 * 未读消息数
 */
export function unreadMessageCount() {
  return request.post('/message/unreadCount', {})
}
