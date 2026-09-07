import JSEncrypt from 'jsencrypt'
import { getPublicKey } from '../api/auth'

/**
 * 获取 RSA 公钥并用它加密明文密码
 * @param {string} plain 明文密码
 * @returns {Promise<string>} Base64 密文
 */
export async function encryptPassword(plain) {
  const res = await getPublicKey()
  const encryptor = new JSEncrypt()
  encryptor.setPublicKey(res.data)
  const encrypted = encryptor.encrypt(plain)
  if (!encrypted) {
    throw new Error('密码加密失败，请刷新页面重试')
  }
  return encrypted
}
