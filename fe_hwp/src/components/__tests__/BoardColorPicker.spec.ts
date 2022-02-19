import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it } from "vitest";
import BoardColorPicker from "../../components/BoardColorPicker.vue";

describe("BoardColorPicker", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("mounts", () => {
    const app = mount(BoardColorPicker, { attachTo: document.body });
    expect(app).not.toBeNaN();
  });
});
