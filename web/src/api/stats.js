import request from './request'

export function getOverview() {
  return request.get('/stats/overview')
}

export function getDirection() {
  return request.get('/stats/direction')
}

export function getOccasion() {
  return request.get('/stats/occasion')
}

export function getTreemap() {
  return request.get('/stats/treemap')
}

export function getMonthly() {
  return request.get('/stats/monthly')
}
