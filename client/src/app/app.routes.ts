import { Routes } from '@angular/router';

export const routes: Routes = [
    { path: '', redirectTo: 'client', pathMatch: 'full' },
    {
        path: 'client',
        loadChildren: () => import("./modules/client/client-routing.module").then(m => m.ClientRoutingModule)
    },
    {
        path: 'admin',
        loadChildren: () => import("./modules/admin/admin-routing.module").then(m => m.AdminRoutingModule)
    },
    {
        path: 'error',
        loadComponent: () => import('./components/shared/error/error.component').then(c => c.ErrorComponent)
    },
    {
        path: '**',
        loadComponent: () => import('./components/shared/not-found/not-found.component').then(c => c.NotFoundComponent)
    }
];
