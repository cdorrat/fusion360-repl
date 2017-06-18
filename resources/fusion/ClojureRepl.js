//Author-Cameron Dorrrat
//Description-Connect Fusion360 to a Clojure Repl\t


// this script just handles the odd Fusion debug support
// then calls the clojurescript run
function run(context) {
   
    "use strict";
    if (adsk.debug === true) {
        /*jslint debug: true*/
        debugger;
        /*jslint debug: false*/
    }

    {{cljs-main}}.run(context);

    adsk.autoTerminate(false);
}
