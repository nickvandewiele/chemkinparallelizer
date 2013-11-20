ChemkinParallelizer
===================

ChemkinParallelizer is a utility that bypasses the Chemkin GUI and runs multiple reactor simulations in parallel. 
This becomes useful when you want to test a particular model over a range of operating conditions, like temperature,
pressure, initial reaction mixture, etc... 

Chemkin already provided this somewhat similar functionality through the "continuations" feature in the GUI, but this code tries to extend this much more. $

How?
- you define an XML file with all the simulations you want to carry out. 
- you specify the kinetic model input file, the reactor configuration files, and the transport file in case that is required. thermo file is supposed to be part of the kinetic model.
- the rest of the processing is done by ChemkinParallelizer. It calls the necessary sub-processes that the Chemkin GUI also calls and makes the temporarily created files find each other.

Next, ChemkinParallelizer runs all the simulations you defined in the XML file. If you dispose of multiple chemkin licenses, you can take advantage of this. ChemkinParallelizer is programmed to run as many simulations in parallel as there are licenses available. It uses as many CPU's at the same time as your computer has. 

ChemkinParallelizer is more flexible than the continuations feature of Chemkin because it supports running multiple reactor configurations at the same time. Moreover, since you specify the reactor configuration files yourself, you can change whatever parameter you want. Finally, ChemkinParallelizer is entirily command-line based, and thus much more prone to automatization in environments like linux.

Right now, the following reactor types are supported (by calling to the respective chemkin solver)
- Plug flow reactor (CKReactorPlugFlow)
- CSTR (CKReactorGenericPSR)
- Batch (CKReactorGenericClosed)
- Flame speed calculations; freely propagating laminar flame (CKReactorFreelyPropagatingFlame)
- Ignition Delay calculations; transient solver, as in shock tube experiments (CKReactorGenericClosed)

Each of the supported reactor models assumes a particular output value to be monitored. For example, in the case
of PFR, the effluent composition will be parsed. This is the summary of the monitored properties as a function of the
reactor type:

- PFR, CSTR, Batch: effluent at outlet, or end time
- Freely propagating laminar flame: flame speed
- Ignition Delay: 1_by_max_dT/dt

See the examples section for some examples.
