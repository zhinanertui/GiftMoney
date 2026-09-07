import request from './request'

export function pageGifts(params) {
  return request.get('/gifts', { params })
}

export function createGift(data) {
  return request.post('/gifts', data)
}

export function updateGift(id, data) {
  return request.put(`/gifts/${id}`, data)
}

export function deleteGift(id) {
  return request.delete(`/gifts/${id}`)
}

export function batchDeleteGifts(ids) {
  return request.post('/gifts/batch-delete', { ids })
}

export function importGifts(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/gifts/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadTemplate() {
  return request.get('/gifts/template', { responseType: 'blob' })
}

export function getContacts(params) {
  return request.get('/gifts/contacts', { params })
}
