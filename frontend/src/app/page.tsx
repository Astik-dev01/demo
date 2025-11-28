import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';

export default function Home() {
  const cookieStore = cookies();
  const accessToken = cookieStore.get('accessToken');

  if (accessToken) {
    // Authenticated users go to dashboard
    redirect('/dashboard');
  } else {
    // Non-authenticated users go to login
    redirect('/login');
  }
}
