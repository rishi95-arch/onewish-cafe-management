document.querySelectorAll('img').forEach(image => image.addEventListener('error', () => image.classList.add('image-missing'), { once: true }));
