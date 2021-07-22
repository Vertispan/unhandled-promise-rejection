package com.vertispan.sample.unhandledrejection.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLButtonElement;
import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

import static elemental2.dom.DomGlobal.*;

public class AppEntryPoint implements EntryPoint {

    // Helper to add a listener to gwt's own iframe window, without a check if it is different from the host page
    @JsMethod(namespace = "<window>")
    private static native void addEventListener(String type, EventListener listener);

    @Override
    public void onModuleLoad() {
        HTMLButtonElement rejectPromiseBtn = (HTMLButtonElement) document.createElement("button");
        rejectPromiseBtn.textContent = "click to reject (from gwt)";
        rejectPromiseBtn.onclick = e -> {
//            console.log("Rejecting a promise in an unhandled way from gwt");
            new Promise<>((resolve, reject) -> {
                setTimeout(reject::onInvoke, 100);
            }).then(happy -> {
//                console.log("this will never happen");
                return Promise.resolve(happy);
            });
            return true;
        };

        HTMLButtonElement throwPromiseBtn = (HTMLButtonElement) document.createElement("button");
        throwPromiseBtn.textContent = "click to throw IllegalStateException (from gwt)";
        throwPromiseBtn.onclick = e -> {
//            console.log("Throwing in a promise in an unhandled way from gwt");
            new Promise<>((resolve, reject) -> {
                setTimeout(resolve::onInvoke, 100);
            }).then(v -> {
                throw new IllegalStateException("thrown exception");
            });
            return true;
        };
        document.getElementById("promises").append(rejectPromiseBtn, throwPromiseBtn);

        // ---

        HTMLButtonElement throwBtn = (HTMLButtonElement) document.createElement("button");
        throwBtn.onclick = e -> {
//            console.log("Throwing in an event handler in an unhandled way from gwt");

            throw new IllegalStateException("throw IllegalStateException");
        };
        throwBtn.textContent = "click to throw in an event handler (from gwt)";

        document.getElementById("throws").append(throwBtn);

        // ---

        addEventListener("unhandledrejection", e -> {
            log("gwt's own reject " + fromObject(((PromiseRejectionEvent) e).reason).getClass());
        });
        window.addEventListener("unhandledrejection", e -> {
            log("host page reject via gwt " + fromObject(((PromiseRejectionEvent) e).reason).getClass());
        });
        window.addEventListener("error", e -> {
            Object error = Js.asPropertyMap(e).get("error");
            if (error == null) {
                error = "null";
            }
            log("gwt noticed a host page error " + fromObject(error).getClass());
        });
        addEventListener("error", e -> {
            Object error = Js.asPropertyMap(e).get("error");
            if (error == null) {
                error = "null";
            }
            log("gwt noticed its own iframe error " + fromObject(error).getClass());
        });
    }

    private static void log(String message) {
        document.getElementById("log").append(Element.AppendNodesUnionType.of(message), Element.AppendNodesUnionType.of(document.createElement("br")));
    }

    // name must be Object instead of PromiseRejectionEvent, because Chrome doesn't use the host page's event constructor...
    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    public static class PromiseRejectionEvent extends Event {
        public Promise<?> promise;
        public Object reason;

        public PromiseRejectionEvent(String type) {
            super(type);
        }
    }
    private static native Throwable fromObject(Object obj) /*-{
        //GWT2 impl using JSNI, see GWT.native.js for the j2cl impl
        return @java.lang.Throwable::of(*)(obj);
    }-*/;
}
