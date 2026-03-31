/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        display: ['"Syne"', 'sans-serif'],
        body: ['"DM Sans"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      colors: {
        void: '#080C14',
        surface: '#0D1117',
        panel: '#161B22',
        border: '#21262D',
        muted: '#30363D',
        subtle: '#8B949E',
        body: '#C9D1D9',
        bright: '#F0F6FC',
        cyan: {
          DEFAULT: '#00D9FF',
          dim: '#00D9FF1A',
          glow: '#00D9FF33',
        },
        emerald: {
          DEFAULT: '#00FF88',
          dim: '#00FF881A',
        },
        amber: {
          DEFAULT: '#FFB700',
          dim: '#FFB7001A',
        },
        rose: {
          DEFAULT: '#FF4D6A',
          dim: '#FF4D6A1A',
        },
        violet: {
          DEFAULT: '#A855F7',
          dim: '#A855F71A',
        }
      },
      backgroundImage: {
        'grid-pattern': `linear-gradient(rgba(0,217,255,0.03) 1px, transparent 1px),
                         linear-gradient(90deg, rgba(0,217,255,0.03) 1px, transparent 1px)`,
      },
      backgroundSize: {
        'grid': '32px 32px',
      },
      boxShadow: {
        'cyan-glow': '0 0 20px rgba(0, 217, 255, 0.15)',
        'emerald-glow': '0 0 20px rgba(0, 255, 136, 0.15)',
        'panel': '0 4px 24px rgba(0, 0, 0, 0.4)',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.4s ease-out',
      },
      keyframes: {
        fadeIn: { from: { opacity: '0' }, to: { opacity: '1' } },
        slideUp: { from: { opacity: '0', transform: 'translateY(12px)' }, to: { opacity: '1', transform: 'translateY(0)' } },
      }
    }
  },
  plugins: [require('@tailwindcss/forms')]
}
