import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { Placeholder } from './components/Placeholder';
import { RequireAuth, RequireRole } from './components/ProtectedRoute';
import { Landing } from './pages/Landing';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Discover } from './pages/Discover';
import { ClientDashboard } from './pages/client/ClientDashboard';
import { ClientProjects } from './pages/client/ClientProjects';
import { JobDetail } from './pages/client/JobDetail';
import { JobForm } from './pages/client/JobForm';
import { CreatorDashboard } from './pages/creator/CreatorDashboard';
import { PortfolioManager } from './pages/creator/PortfolioManager';
import { ProfileEdit } from './pages/creator/ProfileEdit';
import { PublicProfile } from './pages/creator/PublicProfile';
import { JobDetailPage } from './pages/jobs/JobDetailPage';

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Landing />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
            <Route element={<RequireAuth />}>
              <Route path="discover" element={<Discover />} />
              <Route element={<RequireRole role="CLIENT" />}>
                <Route path="client" element={<ClientDashboard />} />
                <Route path="client/jobs/new" element={<JobForm />} />
                <Route path="client/jobs/:id" element={<JobDetail />} />
                <Route path="client/jobs/:id/edit" element={<JobForm />} />
                <Route path="client/projects" element={<ClientProjects />} />
              </Route>
              <Route element={<RequireRole role="CREATOR" />}>
                <Route path="creator" element={<CreatorDashboard />} />
                <Route path="creator/profile" element={<ProfileEdit />} />
                <Route path="creator/portfolio" element={<PortfolioManager />} />
              </Route>
              <Route path="creator/:id" element={<PublicProfile />} />
              <Route path="jobs/:id" element={<JobDetailPage />} />
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
