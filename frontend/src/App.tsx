import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { Placeholder } from './components/Placeholder';
import { RequireAuth, RequireRole } from './components/ProtectedRoute';

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Placeholder title="CreatorHire — landing ships in TASK-021" />} />
            <Route path="login" element={<Placeholder title="Login ships in TASK-021" />} />
            <Route path="register" element={<Placeholder title="Register ships in TASK-021" />} />
            <Route path="discover" element={<Placeholder title="Discovery ships in TASK-022" />} />
            <Route element={<RequireAuth />}>
              <Route element={<RequireRole role="CLIENT" />}>
                <Route path="client/*" element={<Placeholder title="Client dashboard ships in TASK-023" />} />
              </Route>
              <Route element={<RequireRole role="CREATOR" />}>
                <Route path="creator/*" element={<Placeholder title="Creator dashboard ships in TASK-024" />} />
              </Route>
              <Route element={<RequireRole role="ADMIN" />}>
                <Route path="admin/*" element={<Placeholder title="Admin screens ship in TASK-025" />} />
              </Route>
            </Route>
            <Route path="*" element={<Placeholder title="Page not found" />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
