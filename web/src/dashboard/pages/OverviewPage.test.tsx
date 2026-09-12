import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { OverviewPage } from './OverviewPage'
import { stubTenants } from '../../shared/api/stubData'

describe('OverviewPage', () => {
  it('lists every stub tenant by name and slug', () => {
    render(<OverviewPage />)

    for (const tenant of stubTenants) {
      expect(screen.getByText(tenant.name)).toBeInTheDocument()
      expect(screen.getByText(tenant.slug)).toBeInTheDocument()
    }
  })
})
