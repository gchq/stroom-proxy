# Stroom Proxy

_Stroom Proxy_ is typically used as a proxy for _Stroom_ allowing systems to forward events or bundles of events to _Stroom Proxy_ where they will be aggregated and forwarded on to _Stroom_. It has the following features:

* Accepts events POSTed to it.
* Local (transient or permanent) storage of events.
* Aggregation of received events into larger bundles.
* Forwarding of event bundles to _Stroom_ or another _Stroom Proxy_.

_Stroom Proxy_ is flexible and can be configured to operate in a number of different modes depending on the requirements. While _Stroom_ can accept events directly, fronting it with _Stroom Proxy_ provides a degree of separation between the client system and _Stroom_ allowing _Stroom_ to be taken offline without affecting the service of client systems. In such a set up multiple load balanced instances of _Stroom Proxy_ can be used to provide resilience.

The install guide is in the main [Stroom documentation](http://github.com/gchq/stroom-docs).
