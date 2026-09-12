import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { CampaignsPage } from './CampaignsPage'
import { stubCampaigns } from '../../shared/api/stubData'

describe('CampaignsPage', () => {
  it('renders every stub campaign with its status', () => {
    render(<CampaignsPage />)

    for (const campaign of stubCampaigns) {
      expect(screen.getByText(campaign.name)).toBeInTheDocument()
    }
    expect(screen.getAllByText('ACTIVE').length).toBeGreaterThan(0)
    expect(screen.getByText('ENDED')).toBeInTheDocument()
  })
})
