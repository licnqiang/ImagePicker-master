package com.lzy.imagepicker.util;

import ohos.aafwk.ability.Ability;
import ohos.agp.components.Component;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.StateElement;

public class ElementUtil {


    public static class StateElementBuilder {

        private StateElement stateElement;

        public StateElementBuilder() {
            stateElement = new StateElement();
        }

        public StateElementBuilder addState(int[] stateSet, Element element) {
            stateElement.addState(stateSet, element);
            return this;
        }

        public void bind(Component component) {
            component.setBackground(stateElement);
        }

        public StateElement build() {
            return stateElement;
        }
    }

}
