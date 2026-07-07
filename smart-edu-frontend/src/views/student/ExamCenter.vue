<template>
  <div class="exam-center">
    <!-- 考试列表 -->
    <template v-if="!activeExam">
      <div class="page-card">
        <PageHeader title="考试中心" :subtitle="`待考 ${pendingExams.length} 项 · 已完成 ${doneExams.length} 项`" />

        <div v-if="exams.length === 0">
          <EmptyState description="暂无考试安排" />
        </div>

        <GlassCard v-for="e in exams" :key="e.id" :title="e.examName || e.courseName" :gradient-top="true">
          <template #header>
            <div class="exam-header">
              <h3 class="exam-name">{{ e.examName || e.courseName }}</h3>
              <el-tag :type="examStatusType(e)" size="small">{{ examStatusText(e) }}</el-tag>
            </div>
          </template>
          <div class="exam-info">
            <div class="exam-info-item">
              <el-icon :size="14"><Timer /></el-icon>
              <span>{{ formatTime(e.startTime) }} - {{ formatTime(e.endTime) }}</span>
            </div>
            <div class="exam-info-item">
              <el-icon :size="14"><Clock /></el-icon>
              <span>时长 {{ e.durationMinutes }} 分钟</span>
            </div>
            <div class="exam-info-item">
              <el-icon :size="14"><DataLine /></el-icon>
              <span>满分 {{ e.totalScore }} 分</span>
            </div>
          </div>
          <template #footer>
            <div class="exam-actions">
              <el-button v-if="canStart(e)" type="primary" @click="startExam(e)">进入考试</el-button>
              <el-button v-if="canViewResult(e)" @click="viewResult(e)">查看成绩</el-button>
            </div>
          </template>
        </GlassCard>
      </div>
    </template>

    <!-- 考试作答页 -->
    <template v-else-if="examState === 'answering'">
      <div class="page-card exam-page">
        <div class="exam-topbar">
          <h2>{{ activeExam.examName }}</h2>
          <div class="exam-countdown" :class="{ warning: remainingSeconds < 300 }">
            <el-icon><Timer /></el-icon>
            <span>{{ formatCountdown(remainingSeconds) }}</span>
          </div>
        </div>

        <!-- 切屏警告 -->
        <el-alert
          v-if="tabSwitchCount > 0"
          :title="`系统检测到你已离开考试页面 ${tabSwitchCount} 次`"
          :type="tabSwitchCount > 3 ? 'error' : 'warning'"
          :closable="false"
          show-icon
          style="margin-bottom:16px"
        />

        <div v-for="(q, idx) in questions" :key="q.id" class="question-card page-card">
          <div class="q-header">
            <span class="q-num">第 {{ idx + 1 }} 题</span>
            <el-tag size="small" type="info">{{ qTypeLabel(q.questionType) }}</el-tag>
            <span class="q-score">({{ q.score || 5 }}分)</span>
          </div>
          <div class="q-content" v-html="q.content"></div>
          <!-- 单选题 -->
          <el-radio-group v-if="q.questionType === 1" v-model="answers[q.id]" class="q-options">
            <el-radio v-for="(opt, oi) in parseOptions(q.options)" :key="oi" :value="opt[0]" class="q-option">
              {{ opt }}
            </el-radio>
          </el-radio-group>
          <!-- 多选题 -->
          <el-checkbox-group v-else-if="q.questionType === 2" v-model="multiAnswers[q.id]" class="q-options">
            <el-checkbox v-for="(opt, oi) in parseOptions(q.options)" :key="oi" :value="opt[0]" :label="opt[0]" class="q-option">
              {{ opt }}
            </el-checkbox>
          </el-checkbox-group>
          <!-- 判断题 -->
          <el-radio-group v-else-if="q.questionType === 3" v-model="answers[q.id]" class="q-options">
            <el-radio value="A">正确</el-radio>
            <el-radio value="B">错误</el-radio>
          </el-radio-group>
          <!-- 填空题 -->
          <el-input v-else-if="q.questionType === 4" v-model="answers[q.id]" placeholder="请输入答案" class="q-input" />
        </div>

        <div class="exam-submit-bar">
          <el-button @click="activeExam = null; examState = ''">返回列表</el-button>
          <el-button type="primary" size="large" @click="submitExam">交卷</el-button>
        </div>
      </div>
    </template>

    <!-- 考试结果页 -->
    <template v-else-if="examState === 'result'">
      <div class="page-card exam-result">
        <div class="result-icon">✅</div>
        <h2>考试已提交</h2>
        <div class="result-score">
          <div class="score-main">{{ resultData?.objectiveScore || resultData?.finalScore || 0 }}分</div>
          <div class="score-label">客观题得分</div>
        </div>

        <!-- AI 评分详情 — 填空题逐题展示 -->
        <div v-if="aiScoringDetails.length > 0" class="ai-scoring-section">
          <div class="ai-scoring-title">
            <el-tag type="primary" effect="dark" size="small" class="ai-badge">AI 评分</el-tag>
            <span>填空题评分详情</span>
          </div>
          <div v-for="detail in aiScoringDetails" :key="detail.questionId" class="ai-score-item">
            <div class="ai-score-header">
              <span class="ai-q-label">第 {{ detail.questionId }} 题</span>
              <el-tag :type="scoreTagType(detail.score)" size="small">{{ detail.score }} 分</el-tag>
              <span class="ai-feedback-text">{{ detail.feedback }}</span>
            </div>
            <el-collapse v-if="detail.reason" class="ai-reason-collapse">
              <el-collapse-item title="查看评分理由">
                <p class="ai-reason-text">{{ detail.reason }}</p>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>

        <div v-if="resultData?.aiFeedback" class="ai-feedback">
          <h4>AI 评分反馈</h4>
          <pre>{{ resultData.aiFeedback }}</pre>
        </div>
        <el-button type="primary" @click="activeExam = null; examState = ''; fetchExams()">返回考试列表</el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Timer, Clock, DataLine } from '@element-plus/icons-vue'
import { examApi } from '@/api'
import { ElMessage, ElNotification } from 'element-plus'
import GlassCard from '@/components/GlassCard.vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const exams = ref<any[]>([])
const activeExam = ref<any>(null)
const examState = ref('')
const questions = ref<any[]>([])
const answers = ref<Record<number, string>>({})
const multiAnswers = ref<Record<number, string[]>>({})
const remainingSeconds = ref(0)
const resultData = ref<any>(null)
const tabSwitchCount = ref(0)
let countdownTimer: ReturnType<typeof setInterval>
let visibilityHandler: (() => void) | null = null

const pendingExams = ref<any[]>([])
const doneExams = ref<any[]>([])

function parseOptions(opts: string) {
  try { return JSON.parse(opts || '[]') } catch { return [] }
}
function qTypeLabel(t: number) { return ['','单选题','多选题','判断题','填空题'][t] || '' }
function examStatusType(e: any) { return e.status === 2 ? 'info' : e.status === 1 ? 'success' : 'warning' }
function examStatusText(e: any) { return e.status === 2 ? '已结束' : e.status === 1 ? '进行中' : '未开始' }
function canStart(e: any) { return e.status !== 2 }
function canViewResult(_e: any) { return false }
function formatTime(t: string) { return t ? new Date(t).toLocaleString('zh-CN') : '-' }
function formatCountdown(s: number) {
  const m = Math.floor(s / 60); const sec = s % 60
  return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

/** 解析 AI 评分详情 JSON */
const aiScoringDetails = computed(() => {
  try {
    const raw = resultData.value?.aiReason
    if (!raw) return []
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

/** 根据分数返回 el-tag type */
function scoreTagType(score: number) {
  if (score == null) return 'info'
  if (score >= 1) return 'success'
  if (score > 0) return 'warning'
  return 'danger'
}

/** 处理切屏检测 */
function handleVisibilityChange() {
  if (document.hidden) {
    tabSwitchCount.value++
    if (tabSwitchCount.value > 5) {
      ElNotification({
        title: '考试已终止',
        message: `你已离开考试页面 ${tabSwitchCount.value} 次，系统自动交卷。`,
        type: 'error',
        duration: 5000,
      })
      submitExam()
    } else if (tabSwitchCount.value > 3) {
      ElNotification({
        title: '切屏警告',
        message: `系统检测到你已离开考试页面 ${tabSwitchCount.value} 次，请勿切屏，否则将自动交卷！`,
        type: 'warning',
        duration: 4000,
      })
    }
  }
}

async function fetchExams() {
  try {
    exams.value = await examApi.list()
    pendingExams.value = exams.value.filter(e => e.status !== 2)
    doneExams.value = exams.value.filter(e => e.status === 2)
  } catch { exams.value = [] }
}

async function startExam(e: any) {
  try {
    const data = await examApi.start(e.id)
    activeExam.value = { ...e, recordId: data.recordId }
    const qs = await examApi.questions(e.id)
    questions.value = qs || []
    answers.value = {}
    multiAnswers.value = {}
    remainingSeconds.value = (e.durationMinutes || 120) * 60
    examState.value = 'answering'
    tabSwitchCount.value = 0

    // 启动切屏监听
    visibilityHandler = () => handleVisibilityChange()
    document.addEventListener('visibilitychange', visibilityHandler)

    // 启动倒计时
    countdownTimer = setInterval(() => {
      remainingSeconds.value--
      if (remainingSeconds.value <= 0) {
        submitExam()
      }
    }, 1000)
  } catch (err: any) {
    ElMessage.error(err?.message || '开始考试失败')
  }
}

/** 统一交卷方法：手动交卷、超时自动交卷、切屏强制交卷均走此方法 */
async function submitExam() {
  // 清理计时器与监听器
  clearInterval(countdownTimer)
  if (visibilityHandler) {
    document.removeEventListener('visibilitychange', visibilityHandler)
    visibilityHandler = null
  }
  try {
    // 合并单选/判断 + 多选答案
    const answerList = questions.value.map(q => ({
      questionId: q.id,
      answer: q.questionType === 2
        ? (multiAnswers.value[q.id] || []).join(',')
        : (answers.value[q.id] || ''),
    }))
    const data = await examApi.submit(activeExam.value.id, answerList)
    resultData.value = data
    examState.value = 'result'
    ElMessage.success('交卷成功！')
  } catch (err: any) {
    ElMessage.error(err?.message || '交卷失败')
  }
}

async function viewResult(e: any) {
  try {
    resultData.value = await examApi.myResult(e.id)
    activeExam.value = e
    examState.value = 'result'
  } catch { ElMessage.error('获取成绩失败') }
}

onMounted(fetchExams)

/** 组件卸载时清理所有资源 */
onUnmounted(() => {
  clearInterval(countdownTimer)
  if (visibilityHandler) {
    document.removeEventListener('visibilitychange', visibilityHandler)
    visibilityHandler = null
  }
})
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
.page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 16px; }

.exam-header { display: flex; justify-content: space-between; align-items: center; }
.exam-name { font-size: 16px; font-weight: 600; margin: 0; color: var(--color-text); }
.exam-info { display: flex; gap: 20px; flex-wrap: wrap; margin: 12px 0; }
.exam-info-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: var(--color-text-secondary); }
.exam-actions { display: flex; gap: 8px; justify-content: flex-end; }

.exam-topbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  h2 { margin: 0; font-size: 18px; }
}
.exam-countdown { display: flex; align-items: center; gap: 6px; font-size: 18px; font-weight: 700; font-family: 'Space Grotesk', monospace; color: var(--color-brand-light);
  &.warning { color: #f87171; animation: breathe 1s infinite; }
}

.question-card { margin-bottom: 16px; padding: 20px; }
.q-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.q-num { font-weight: 600; font-size: 14px; color: var(--color-text); }
.q-score { font-size: 12px; color: var(--color-text-muted); }
.q-content { font-size: 14px; color: var(--color-text); line-height: 1.6; margin-bottom: 12px; }
.q-options { display: flex; flex-direction: column; gap: 8px; }
.q-option { font-size: 13px; color: var(--color-text-secondary); }
.q-input { max-width: 400px; }

.exam-submit-bar { display: flex; justify-content: space-between; margin-top: 24px; padding: 16px 0; }

.exam-result { text-align: center; padding: 60px 20px; }
.result-icon { font-size: 56px; margin-bottom: 16px; }
.result-score { margin: 24px 0; }
.score-main { font-size: 48px; font-weight: 700; color: var(--color-brand-light); font-family: 'Space Grotesk', monospace; }
.score-label { font-size: 13px; color: var(--color-text-muted); margin-top: 4px; }
.ai-feedback { text-align: left; background: rgba(139,92,246,0.05); padding: 16px; border-radius: 10px; margin: 16px 0;
  h4 { margin: 0 0 8px; font-size: 14px; }
  pre { margin: 0; font-size: 12px; color: var(--color-text-secondary); white-space: pre-wrap; }
}

/* AI 评分逐题详情 */
.ai-scoring-section {
  text-align: left;
  background: rgba(64, 158, 255, 0.04);
  border: 1px solid rgba(64, 158, 255, 0.15);
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
}
.ai-scoring-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 16px;
}
.ai-badge {
  font-weight: 600;
}
.ai-score-item {
  padding: 10px 0;
  border-bottom: 1px solid rgba(64, 158, 255, 0.08);
  &:last-child { border-bottom: none; }
}
.ai-score-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.ai-q-label {
  color: var(--color-text);
  font-weight: 500;
}
.ai-feedback-text {
  color: var(--color-text-secondary);
}
.ai-reason-collapse {
  margin-top: 6px;
  :deep(.el-collapse-item__header) {
    font-size: 12px;
    color: var(--color-brand-light);
    height: 32px;
    line-height: 32px;
    border: none;
    background: transparent;
  }
  :deep(.el-collapse-item__wrap) {
    background: transparent;
    border: none;
  }
  :deep(.el-collapse-item__content) {
    font-size: 12px;
    color: var(--color-text-secondary);
    padding-bottom: 4px;
  }
}
.ai-reason-text {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}
</style>
