<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
            <div class="stat-label">总资产数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card borrowed">
          <div class="stat-icon">
            <el-icon><DocumentCopy /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.borrowedCount || 0 }}</div>
            <div class="stat-label">领用中</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card maintenance">
          <div class="stat-icon">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.maintenanceCount || 0 }}</div>
            <div class="stat-label">维修中</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card instock">
          <div class="stat-icon">
            <el-icon><Checked /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.inStockCount || 0 }}</div>
            <div class="stat-label">在库</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <div class="stat-card scrap">
          <div class="stat-icon">
            <el-icon><Delete /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.scrapCount || 0 }}</div>
            <div class="stat-label">已报废</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAssetStatistics } from '@/api/asset'

const statistics = ref({
  totalCount: 0,
  inStockCount: 0,
  borrowedCount: 0,
  maintenanceCount: 0,
  scrapCount: 0
})

const fetchStatistics = async () => {
  try {
    const res = await getAssetStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 30px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #34495e 0%, #2c3e50 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(52, 73, 94, 0.2);
  background: rgba(255, 255, 255, 0.95);
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);
  color: #fff;
}

.stat-card.borrowed .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
}

.stat-card.maintenance .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.stat-card.instock .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.stat-card.scrap .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}
</style>
