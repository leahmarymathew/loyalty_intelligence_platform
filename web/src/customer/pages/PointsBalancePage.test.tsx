import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { PointsBalancePage } from './PointsBalancePage'
import { computeStubBalance, stubCustomer } from '../../shared/api/stubData'
import { formatPoints } from '../../shared/format'

describe('PointsBalancePage', () => {
  it('shows the customer\'s current points balance', () => {
    render(<PointsBalancePage />)

    const expectedBalance = formatPoints(computeStubBalance(stubCustomer.id).balance)
    expect(screen.getByText(expectedBalance)).toBeInTheDocument()
    expect(screen.getByText('points')).toBeInTheDocument()
  })

  it('shows the customer\'s current tier', () => {
    render(<PointsBalancePage />)

    expect(screen.getByText('Silver tier')).toBeInTheDocument()
  })

  it('lists recent ledger activity with signed point values', () => {
    render(<PointsBalancePage />)

    expect(screen.getByText('Recent activity')).toBeInTheDocument()
    expect(screen.getByText('+420')).toBeInTheDocument()
    expect(screen.getByText('-200')).toBeInTheDocument()
  })
})
