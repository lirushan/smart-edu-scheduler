<template>
  <div class="skeleton-page page-card">
    <div class="skeleton-page__inner">
      <div class="skeleton-icon">🏗️</div>
      <h2>{{ title }}</h2>
      <p class="skeleton-desc">功能开发中，敬请期待后续版本...</p>
      <div class="skeleton-progress">
        <div class="skeleton-bar">
          <div class="skeleton-fill"></div>
        </div>
        <span class="skeleton-text">规划已就绪 · 路由和菜单已接入</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<style lang="scss" scoped>
.skeleton-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;

  &__inner {
    max-width: 400px;
  }

  .skeleton-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  h2 {
    font-size: 20px;
    color: var(--color-text);
    margin-bottom: 8px;
    font-weight: 600;
  }

  .skeleton-desc {
    color: var(--color-text-muted);
    font-size: 14px;
    margin-bottom: 24px;
  }

  .skeleton-progress {
    .skeleton-bar {
      height: 4px;
      background: rgba(139, 92, 246, 0.1);
      border-radius: 2px;
      overflow: hidden;
      margin-bottom: 8px;
    }
    .skeleton-fill {
      height: 100%;
      width: 30%;
      background: linear-gradient(90deg, #8b5cf6, #667eea);
      border-radius: 2px;
      animation: skeletonShimmer 2s ease-in-out infinite;
    }
    .skeleton-text {
      font-size: 11px;
      color: var(--color-text-muted);
    }
  }
}

@keyframes skeletonShimmer {
  0%, 100% { width: 30%; }
  50% { width: 60%; }
}
</style>
