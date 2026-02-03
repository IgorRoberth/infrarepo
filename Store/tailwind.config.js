/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,js}"],
  theme: {
    extend: {
      colors: {
        background: '#ffffff',
        foreground: '#030213',
        primary: '#030213',
        'primary-foreground': '#ffffff',
        secondary: '#f3f3f5',
        'secondary-foreground': '#030213',
        muted: '#ececf0',
        'muted-foreground': '#717182',
        border: 'rgba(0, 0, 0, 0.1)',
      }
    },
  },
  plugins: [],
}