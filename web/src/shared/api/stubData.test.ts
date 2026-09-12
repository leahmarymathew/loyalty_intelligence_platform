import { describe, expect, it } from 'vitest'
import { computeStubBalance, stubCustomer, stubLedgerEntries } from './stubData'

describe('computeStubBalance', () => {
  it('sums ledger entry points for the given customer, matching core-api\'s SUM(points) derivation', () => {
    const expected = stubLedgerEntries
      .filter((entry) => entry.customerId === stubCustomer.id)
      .reduce((sum, entry) => sum + entry.points, 0)

    const result = computeStubBalance(stubCustomer.id)

    expect(result.balance).toBe(expected)
    expect(result.customerId).toBe(stubCustomer.id)
  })

  it('returns zero for a customer with no ledger entries', () => {
    const result = computeStubBalance('unknown-customer-id')

    expect(result.balance).toBe(0)
  })
})
