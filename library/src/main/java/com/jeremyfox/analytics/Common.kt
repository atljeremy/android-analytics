package com.jeremyfox.analytics

import java.util.*

class CommonAnalyticsProviderChain: Analytics.ProviderChain {
    override val chain: MutableList<Analytics.Provider> = ArrayList()
}

class CommonAnalyticsProcessorChain: Analytics.ProcessorChain {
    override val chain: MutableList<Analytics.Processor> = ArrayList()
}

class CommonAnalyticsConsumer: Analytics.Consumer {
    override val providerChain = CommonAnalyticsProviderChain()
    override val processorChain = CommonAnalyticsProcessorChain()
}