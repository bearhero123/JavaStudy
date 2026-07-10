import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('请求错误：', error)

    ElMessage.error(error.response?.data?.msg || '请求失败，请检查后端服务是否启动')

    return Promise.reject(error)
  },
)

export default request
