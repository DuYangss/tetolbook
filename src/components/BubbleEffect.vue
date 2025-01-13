<template>
  <canvas ref="canvas" class="bubble-canvas"></canvas>
</template>

<script>
export default {
  name: 'BubbleEffect',
  mounted() {
    this.initCanvas()
  },
  methods: {
    initCanvas() {
      const canvas = this.$refs.canvas
      const ctx = canvas.getContext('2d')
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
      
      let bubbles = []
      
      // 自动生成底部气泡
      setInterval(() => {
        const x = Math.random() * canvas.width
        const size = Math.random() * 8 + 6  // 更大的气泡尺寸
        bubbles.push({
          x: x,
          y: canvas.height + size,  // 从底部开始
          size: size,
          speedX: Math.random() * 1 - 0.5,
          speedY: -Math.random() * 2 - 1,  // 向上移动
          opacity: 1
        })
      }, 100)  // 每100ms生成一个气泡
      
      // 鼠标移动时创建气泡
      document.addEventListener('mousemove', (e) => {
        if (Math.random() < 0.3) {
          bubbles.push({
            x: e.clientX,
            y: e.clientY,
            size: Math.random() * 4 + 2,
            speedX: Math.random() * 2 - 1,
            speedY: -Math.random() * 1.5 - 1,  // 稍微加快上升速度
            opacity: 1
          })
        }
      })
      
      // 鼠标点击时创建多个气泡
      document.addEventListener('click', (e) => {
        for (let i = 0; i < 15; i++) {
          const angle = (Math.PI * 2 / 15) * i
          bubbles.push({
            x: e.clientX,
            y: e.clientY,
            size: Math.random() * 6 + 3,
            speedX: Math.cos(angle) * (Math.random() * 2 + 2),
            speedY: Math.sin(angle) * (Math.random() * 2 + 2),
            opacity: 1
          })
        }
      })
      
      function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        
        bubbles = bubbles.filter(bubble => {
          bubble.x += bubble.speedX
          bubble.y += bubble.speedY
          bubble.opacity -= 0.008  // 稍微降低淡出速度
          
          if (bubble.opacity <= 0) return false
          
          ctx.beginPath()
          ctx.arc(bubble.x, bubble.y, bubble.size, 0, Math.PI * 2)
          ctx.fillStyle = `rgba(255, 255, 255, ${bubble.opacity})`
          ctx.fill()
          
          return bubble.y > -bubble.size
        })
        
        requestAnimationFrame(animate)
      }
      
      animate()
      
      // 窗口大小改变时调整canvas大小
      window.addEventListener('resize', () => {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
      })
    }
  }
}
</script>

<style scoped>
.bubble-canvas {
  position: fixed;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 100;
}
</style> 