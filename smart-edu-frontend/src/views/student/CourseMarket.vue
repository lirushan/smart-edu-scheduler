<template>
  <div class="page-card course-market">
    <PageHeader title="课程广场" subtitle="浏览并选择本学期课程" />

    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索课程名或教师..." clearable class="search-input" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="category" placeholder="课程分类" clearable class="filter-select">
        <el-option label="计算机科学" value="计算机科学" />
        <el-option label="数学" value="数学" />
        <el-option label="外语" value="外语" />
        <el-option label="体育" value="体育" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <!-- 课程列表 -->
    <LoadingState v-if="loading" :rows="4" />
    <EmptyState v-else-if="offerings.length === 0" description="暂无符合条件的课程" />
    <div v-else class="course-grid">
      <div v-for="o in offerings" :key="o.id" class="course-card page-card card-hover">
        <div class="course-header">
          <div class="course-category tag">{{ o.category || '通识' }}</div>
          <div class="course-credit">{{ o.credit }} 学分</div>
        </div>
        <h3 class="course-name">{{ o.courseName }}</h3>
        <div class="course-meta">
          <div class="meta-item">
            <el-icon :size="14"><UserFilled /></el-icon>
            <span>{{ o.teacherName }}</span>
          </div>
          <div class="meta-item">
            <el-icon :size="14"><Calendar /></el-icon>
            <span>周{{ dayLabel(o.weekday) }} {{ o.periodStart }}-{{ o.periodEnd }}节</span>
          </div>
          <div class="meta-item">
            <el-icon :size="14"><Location /></el-icon>
            <span>{{ o.location }}</span>
          </div>
        </div>
        <div class="course-footer">
          <div class="capacity-bar">
            <div class="capacity-fill" :style="{ width: capacityPercent(o) + '%' }"></div>
          </div>
          <span class="capacity-text">{{ o.enrolledCount }}/{{ o.capacity }}</span>
          <el-button type="primary" size="small" :disabled="o.enrolledCount >= o.capacity" @click="handleEnroll(o)">
            {{ o.enrolledCount >= o.capacity ? '已满' : '立即选课' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-bar">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[8, 12, 20]"
        layout="total, sizes, prev, pager, next"
        @change="fetchOfferings"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, UserFilled, Calendar, Location } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import { courseApi, enrollmentApi } from '@/api'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const category = ref('')
const page = ref(1)
const size = ref(12)
const total = ref(0)
const loading = ref(false)
const offerings = ref<any[]>([])

const dayLabel = (d: number) => ['','一','二','三','四','五','六','日'][d] || ''
const capacityPercent = (o: any) => o.capacity > 0 ? Math.round((o.enrolledCount / o.capacity) * 100) : 0

async function fetchOfferings() {
  loading.value = true
  try {
    const data = await courseApi.list({ page: page.value, size: size.value, keyword: keyword.value, category: category.value })
    offerings.value = data.records || []
    total.value = data.total || 0
  } catch {
    // Mock fallback
    offerings.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  fetchOfferings()
}

async function handleEnroll(o: any) {
  try {
    await enrollmentApi.enroll(o.id)
    ElMessage.success('选课成功！')
    fetchOfferings()
  } catch (e: any) {
    ElMessage.error(e?.message || '选课失败')
  }
}

onMounted(fetchOfferings)
</script>

<style lang="scss" scoped>
.course-market {
  .search-input { width: 280px; }
  .filter-select { width: 160px; }

  .course-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 16px;
  }

  .course-card {
    padding: 18px 20px;
    display: flex;
    flex-direction: column;
    gap: 10px;

    .course-header {
      display: flex; justify-content: space-between; align-items: center;
      .tag { background: rgba(139,92,246,0.1); color: var(--color-brand-light); padding: 2px 10px; border-radius: 20px; font-size: 11px; font-weight: 500; }
      .course-credit { font-size: 12px; color: var(--color-text-muted); }
    }

    .course-name { font-size: 16px; font-weight: 600; margin: 0; color: var(--color-text); }

    .course-meta {
      display: flex; flex-direction: column; gap: 6px;
      .meta-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--color-text-secondary); }
    }

    .course-footer {
      display: flex; align-items: center; gap: 10px; margin-top: 4px;
      .capacity-bar { flex: 1; height: 4px; background: rgba(139,92,246,0.1); border-radius: 2px; overflow: hidden; }
      .capacity-fill { height: 100%; background: linear-gradient(90deg, #8b5cf6, #667eea); border-radius: 2px; }
      .capacity-text { font-size: 11px; color: var(--color-text-muted); white-space: nowrap; }
    }
  }

  .loading-wrap { display: none; }
  .empty-hint { display: none; }
}
</style>
