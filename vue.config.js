const { defineConfig } = require('@vue/cli-service')
const DefineOptions = require('unplugin-vue-define-options/webpack')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8888
  },
  configureWebpack: {
    plugins: [
      DefineOptions()
    ]
  }
})
