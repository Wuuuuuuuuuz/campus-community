<template>
  <div class="ai-chat-float">
    <Transition name="panel">
      <div v-if="ai.isOpen" class="chat-panel">
        <div class="chat-header">
          <span>AI 助手</span>
          <div class="chat-header-actions">
            <el-button size="small" text @click="ai.clear" :disabled="ai.isLoading">清空</el-button>
            <el-button size="small" text @click="ai.toggle">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>

        <div class="chat-messages" ref="msgListRef">
          <div v-if="ai.messages.length === 0" class="chat-empty">
            有问题尽管问我，比如帖子内容、校园生活等
          </div>
          <div
            v-for="(msg, i) in ai.messages"
            :key="i"
            :class="['chat-msg', msg.role]"
          >
            <div class="msg-content" v-html="renderMsg(msg)"></div>
          </div>
          <div v-if="ai.toolStatus" class="chat-tool-status">
            {{ ai.toolStatus }}...
          </div>
          <div v-else-if="ai.isLoading" class="chat-loading">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            :disabled="ai.isLoading"
            resize="none"
            @keydown.enter.exact.prevent="handleSend"
          />
          <el-button
            v-if="ai.isLoading"
            type="danger"
            size="small"
            @click="ai.stop"
            style="margin-top: 6px;"
          >
            停止
          </el-button>
          <el-button
            v-else
            type="primary"
            size="small"
            @click="handleSend"
            :disabled="!inputText.trim()"
            style="margin-top: 6px;"
          >
            发送
          </el-button>
        </div>
      </div>
    </Transition>

    <div class="chat-bubble" @click="ai.toggle">
      <el-icon v-if="!ai.isOpen" :size="24"><ChatDotRound /></el-icon>
      <el-icon v-else :size="24"><Close /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ChatDotRound, Close } from '@element-plus/icons-vue'
import { useAiStore } from '../stores/ai'
import { marked } from 'marked'

const ai = useAiStore()
const inputText = ref('')
const msgListRef = ref(null)

function renderMsg(msg) {
  if (msg.role === 'assistant') {
    return marked(msg.content || '')
  }
  return escapeHtml(msg.content || '')
}

function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  await ai.send(text)
  scrollBottom()
}

function scrollBottom() {
  nextTick(() => {
    const el = msgListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

watch(() => ai.messages.length, scrollBottom)
watch(() => {
  const msgs = ai.messages
  return msgs.length ? msgs[msgs.length - 1]?.content : ''
}, scrollBottom)
</script>

<style scoped>
.ai-chat-float {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
}

.chat-bubble {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s;
}
.chat-bubble:hover { transform: scale(1.08); }

.chat-panel {
  position: absolute;
  right: 0;
  bottom: 68px;
  width: 400px;
  height: 560px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  font-size: 15px;
}
.chat-header-actions { display: flex; gap: 4px; }

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  background: #fafafa;
}
.chat-empty {
  text-align: center;
  color: #aaa;
  font-size: 13px;
  margin-top: 60px;
}

.chat-msg {
  margin-bottom: 14px;
  display: flex;
}
.chat-msg.user { justify-content: flex-end; }
.chat-msg.assistant { justify-content: flex-start; }

.chat-msg .msg-content {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}
.chat-msg.user .msg-content {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.chat-msg.assistant .msg-content {
  background: #fff;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 4px;
}
.chat-msg.assistant .msg-content :deep(p) { margin: 0 0 6px; }
.chat-msg.assistant .msg-content :deep(p:last-child) { margin-bottom: 0; }
.chat-msg.assistant .msg-content :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}
.chat-msg.assistant .msg-content :deep(pre) {
  background: #f0f0f0;
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
}
.chat-msg.assistant .msg-content :deep(pre code) {
  background: none;
  padding: 0;
}

.chat-tool-status {
  color: #409eff;
  font-size: 13px;
  padding: 6px 14px;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.chat-loading {
  display: flex;
  gap: 5px;
  padding: 8px 14px;
}
.chat-loading .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #bbb;
  animation: bounce 1.2s infinite;
}
.chat-loading .dot:nth-child(2) { animation-delay: 0.2s; }
.chat-loading .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); }
  40% { transform: scale(1); }
}

.chat-input {
  padding: 10px 14px;
  border-top: 1px solid #ebeef5;
}
.chat-input :deep(.el-textarea__inner) {
  border-radius: 8px;
}

.panel-enter-active, .panel-leave-active {
  transition: all 0.25s ease;
}
.panel-enter-from, .panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.95);
}
</style>
