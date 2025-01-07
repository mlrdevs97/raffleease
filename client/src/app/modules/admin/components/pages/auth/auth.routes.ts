import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () => import('./auth.component').then(c => c.AuthComponent),
        children: [
            { path: '', redirectTo: 'login', pathMatch: 'full' },    
            { 
                path: 'login',
                loadComponent: () => import('./login/login.component').then(c => c.LoginComponent)
            },
            { 
                path: 'register',
                loadComponent: () => import('./register/register.component').then(c => c.RegisterComponent)
            }
        ]
    }
];