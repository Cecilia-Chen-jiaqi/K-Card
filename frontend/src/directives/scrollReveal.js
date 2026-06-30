export const scrollReveal = {
  mounted(el, binding) {
    const index = binding.value ?? 0;
    el.classList.add('kc-reveal');
    el.style.setProperty('--reveal-delay', `${index * 0.07}s`);
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          el.classList.add('is-visible');
          observer.unobserve(el);
        }
      },
      { threshold: 0.06, rootMargin: '0px 0px -40px 0px' },
    );
    observer.observe(el);
    el._scrollRevealObserver = observer;
  },
  unmounted(el) {
    el._scrollRevealObserver?.disconnect();
  },
};
