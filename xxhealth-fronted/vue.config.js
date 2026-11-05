const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  productionSourceMap: false,
  devServer: {
    port: 8089,
    proxy: {
      // 开发环境代理：/api/ciReport/listCiReport -> http://localhost:8088/tijiancms/ciReport/listCiReport
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        pathRewrite: { '^/api': '/tijiancms' }
      }
    }
  }
})
