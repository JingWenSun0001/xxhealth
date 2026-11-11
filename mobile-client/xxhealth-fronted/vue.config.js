const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {
      '/tijian': {
        target: process.env.VUE_APP_PROXY_TARGET || 'http://localhost:8088',
        changeOrigin: true,
        pathRewrite: { '^/tijian': '/tijiancms' }
      }
    }
  }
})
