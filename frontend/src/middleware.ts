import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const publicPaths = ['/login', '/register', '/forgot-password', '/reset-password'];
const authPaths = ['/login', '/register'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const accessToken = request.cookies.get('accessToken')?.value;

  // Check if it's a public path
  const isPublicPath = publicPaths.some((path) => pathname.startsWith(path));
  const isAuthPath = authPaths.some((path) => pathname.startsWith(path));

  // If user is authenticated and trying to access auth pages, redirect to dashboard
  if (accessToken && isAuthPath) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  // If user is not authenticated and trying to access protected route
  if (!accessToken && !isPublicPath) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico|.*\\..*).*)'],
};
