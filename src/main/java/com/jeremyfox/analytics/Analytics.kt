package com.jeremyfox.analytics

interface Analytics {
    interface Event {
        var eventName: String
        var data: Map<String, Any>?

        fun include(data: Map<String, Any>): Event {
            this.data = data
            return this
        }
    }

    interface Chain<A, B> {
        val chain: MutableList<A>
        fun executeChain(obj: B): B
        fun register(child: A): Boolean {
            return chain.add(child)
        }
        fun registerAll(children: List<A>): Boolean {
            return chain.addAll(children)
        }
        fun deregister(child: A): Boolean {
            return chain.remove(child)
        }
        fun deregisterAll(children: List<A>): Boolean {
            return chain.removeAll(children)
        }
    }

    interface Provider {
        fun track(event: Event)
        fun setGlobal(data: Map<String, String>)
    }
    interface ProviderChain: Chain<Provider, Event> {
        override fun executeChain(obj: Event): Event {
            return obj.apply { chain.forEach {
                it.track(this)
            } }
        }
    }

    interface Processor {
        fun process(event: Event): Event
    }
    interface ProcessorChain: Chain<Processor, Event> {
        override fun executeChain(obj: Event): Event {
            return chain.fold(obj) { e, processor ->
                processor.process(e)
            }
        }
    }

    interface Consumer {
        val providerChain: ProviderChain
        val processorChain: ProcessorChain?

        fun consume(event: Event) {
            val e = processorChain?.executeChain(event) ?: event
            providerChain.executeChain(e)
        }

        fun register(provider: Provider): Boolean {
            return providerChain.register(provider)
        }
        fun registerAllProviders(providers: List<Provider>): Boolean {
            return providerChain.registerAll(providers)
        }
        fun deregister(provider: Provider): Boolean {
            return providerChain.deregister(provider)
        }
        fun deregisterAllProviders(providers: List<Provider>): Boolean {
            return providerChain.deregisterAll(providers)
        }

        fun register(processor: Processor): Boolean {
            return processorChain?.register(processor) ?: false
        }
        fun registerAllProcessors(processors: List<Processor>): Boolean {
            return processorChain?.registerAll(processors) ?: false
        }
        fun deregister(processor: Processor): Boolean {
            return processorChain?.deregister(processor) ?: false
        }
        fun deregisterAllProcessors(processors: List<Processor>): Boolean {
            return processorChain?.deregisterAll(processors) ?: false
        }
    }
}