package com.acm.acmwebsite.feature.seasons;

// Implemented by features that refer to season periods, so the calendar knows a period is in use
// and refuses to delete it. Add a bean for each new feature that works per period.
public interface PeriodUsage {

    boolean isPeriodInUse(Long periodId);

    // Shown to the admin, for example "badges were awarded for it".
    String usageDescription();
}
