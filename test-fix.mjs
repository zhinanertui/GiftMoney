import crypto from 'crypto'
import http from 'http'

function request(path, method = 'GET', body = null, token = null) {
  return new Promise((resolve, reject) => {
    const options = { host: '127.0.0.1', port: 8080, path, method, headers: { 'Content-Type': 'application/json' } }
    if (token) options.headers['satoken'] = token
    const req = http.request(options, (res) => {
      let data = ''
      res.on('data', (c) => (data += c))
      res.on('end', () => resolve({ status: res.statusCode, body: JSON.parse(data) }))
    })
    req.on('error', reject)
    if (body) req.write(JSON.stringify(body))
    req.end()
  })
}

async function encrypt(plain) {
  const keyRes = await request('/api/auth/public-key')
  return crypto.publicEncrypt({ key: keyRes.body.data, padding: crypto.constants.RSA_PKCS1_PADDING }, Buffer.from(plain)).toString('base64')
}

// 1. xsw 登录，验证菜单
const xswLogin = await request('/api/auth/login', 'POST', { username: 'xsw', password: await encrypt('tingnishuo.A1') })
console.log('XSW LOGIN:', xswLogin.body.code)
const xswMenus = await request('/api/auth/menus', 'GET', null, xswLogin.body.data.tokenValue)
console.log('XSW roles:', xswMenus.body.data.roles.join(','))
console.log('XSW menus:', JSON.stringify(xswMenus.body.data.menus).includes('礼单管理') ? '礼单管理+统计分析 ✓' : '空')

// 2. 新注册用户默认分配租户角色
const newUser = 'testreg_' + Date.now()
const reg = await request('/api/auth/register', 'POST', { username: newUser, password: await encrypt('test123456'), email: null })
console.log('REGISTER:', reg.body.code)
const regMenus = await request('/api/auth/menus', 'GET', null, reg.body.data.tokenValue)
console.log('NEW USER roles:', regMenus.body.data.roles.join(','))
console.log('NEW USER has menus:', regMenus.body.data.menus.length > 0 ? 'yes ✓' : 'NO')
console.log('__CLEANUP_USER__', newUser)
