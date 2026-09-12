import { describe, expect, it } from 'vitest'
import { formatDate, formatPoints } from './format'

describe('formatPoints', () => {
  it('adds thousands separators', () => {
    expect(formatPoints(1234567)).toBe('1,234,567')
  })

  it('formats negative point values (ledger debits)', () => {
    expect(formatPoints(-200)).toBe('-200')
  })
})

describe('formatDate', () => {
  it('renders an ISO instant as a short human-readable date', () => {
    expect(formatDate('2026-01-03T00:00:00Z')).toBe('Jan 3, 2026')
  })
})
