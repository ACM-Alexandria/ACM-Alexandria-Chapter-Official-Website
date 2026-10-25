package com.acm.acmwebsite.feature.dto;

/**
 * Chapter totals shown on the public home page.
 *
 * @param users                every account on the site
 * @param members              accounts that are chapter members (members and every board role)
 * @param eventRegistrations   registrations for events, all time
 * @param programRegistrations registrations for programs, all time
 */
public record PublicStatsDto(long users, long members, long eventRegistrations, long programRegistrations) {
}
